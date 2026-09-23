package alix.common.data.security.email;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.utils.AlixCache;
import alix.common.utils.netty.NettyServerTransport;
import com.google.common.cache.Cache;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Optional built-in webserver that lets a player verify their email by clicking a link in the verification email,
 * instead of having to type a code into the game. Disabled by default (see 'enable-web-verification' in
 * email-config.yml) since it requires a network port that must be reachable from the internet - the operator is
 * expected to set 'web-verification-public-url' correctly (typically behind their own reverse proxy/domain).
 * <p>
 * Built directly on Netty (rather than the JDK's com.sun.net.httpserver) since Netty is already a hard
 * dependency of the host proxy this plugin runs on - reusing it avoids pulling in a second, separate HTTP
 * stack, and lets this pick up the fastest transport actually available (io_uring/epoll on Linux, see
 * {@link NettyServerTransport}) instead of the JDK server's plain blocking-socket-per-thread model.
 * <p>
 * Security notes: tokens are single-use, generated via SecureRandom (32 random bytes - not the same, much weaker,
 * generator used for the in-game 6-digit code), and expire automatically after 'web-verification-token-expiry-minutes'.
 * A token only ever maps to the exact pending verification it was generated for. There are two kinds of link this
 * class can hand out (see the two createVerificationLink() overloads): one only ever attaches an email to an
 * EXISTING account by name - it never creates accounts or changes passwords; the other completes a PENDING
 * registration (see EmailHandler#sendVerifyMailForPendingRegistration) - it never creates an account either on
 * its own, since that only happens if the underlying verification code (the same one "/verifyemail <code>"
 * would need) still matches at the moment the link is clicked.
 */
public final class WebVerificationServer {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Cache<String, PendingWebVerification> TOKENS = AlixCache.<String, PendingWebVerification>newBuilder()
            .maximumSize(2048)
            .expireAfterWrite(Math.max(1, EmailConfig.INSTANCE.webVerificationTokenExpiryMinutes), TimeUnit.MINUTES)
            .build();

    private static EventLoopGroup bossGroup, workerGroup;
    private static Channel serverChannel;

    private WebVerificationServer() {
    }

    /**
     * Starts the webserver if 'enable-web-verification' is set in email-config.yml. Safe to call multiple times -
     * a no-op if already running or disabled. Should be called once on plugin enable.
     */
    public static synchronized void startIfEnabled() {
        if (!EmailConfig.INSTANCE.enableWebVerification || serverChannel != null) return;

        NettyServerTransport transport = NettyServerTransport.INSTANCE;
        var address = new InetSocketAddress(EmailConfig.INSTANCE.webVerificationBindAddress, EmailConfig.INSTANCE.webVerificationPort);

        try {
            bossGroup = transport.newEventLoopGroup(1, new DefaultThreadFactory("alix-web-verify-boss"));
            workerGroup = transport.newEventLoopGroup(2, new DefaultThreadFactory("alix-web-verify-worker"));

            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(transport.serverChannelClass)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //FUNCTIONALITY (audit, 2026-09-24): without this, a client that opens the
                            //connection and then sends nothing (or trickles bytes - classic slowloris) is
                            //held open indefinitely, since HttpServerCodec/HttpObjectAggregator don't time
                            //idle connections out on their own - matches the ReadTimeoutHandler(30) already
                            //used by every other Netty listener in this codebase.
                            ch.pipeline().addLast(new ReadTimeoutHandler(30));
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(1 << 16));
                            ch.pipeline().addLast(new RequestHandler());
                        }
                    });

            serverChannel = bootstrap.bind(address).sync().channel();
            AlixCommonMain.logInfo("Web email verification server started on " + address + " (transport: " + transport.name + ")");
        } catch (Exception e) {
            AlixCommonMain.logWarning("Could not start the web email verification server (is the port already in use?): " + e.getMessage());
            stop();
        }
    }

    /**
     * Stops the webserver, if running. Should be called once on plugin disable.
     */
    public static synchronized void stop() {
        if (serverChannel != null) {
            serverChannel.close();
            serverChannel = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
    }

    /**
     * @param playerName the account this link will verify the email for
     * @param email      the email address being verified
     * @return a full, clickable verification URL, or empty if web verification is disabled or misconfigured (in which case a warning is logged)
     */
    public static Optional<String> createVerificationLink(String playerName, String email) {
        return createLink(new PendingWebVerification(playerName, email, null));
    }

    /**
     * Same as {@link #createVerificationLink(String, String)}, but for a pending registration that has no
     * account yet - instead of attaching the email to an existing account by name, this runs {@code onVerified}
     * when the link is clicked, and the response page reflects whatever it returns. See
     * {@link EmailHandler#sendVerifyMailForPendingRegistration} for how the check itself stays tied to the
     * same single-use verification code the in-game "/verifyemail <code>" path uses, rather than trusting the
     * token alone as a separate grant.
     *
     * @param email      the email address being verified
     * @param onVerified called (on this server's own thread) when the link is clicked - should return whether
     *                    verification actually succeeded, and is responsible for its own thread-safety if it
     *                    needs to touch state that isn't safe to touch from an arbitrary thread
     * @return a full, clickable verification URL, or empty if web verification is disabled or misconfigured (in which case a warning is logged)
     */
    public static Optional<String> createVerificationLink(String email, BooleanSupplier onVerified) {
        return createLink(new PendingWebVerification(null, email, onVerified));
    }

    private static Optional<String> createLink(PendingWebVerification pending) {
        if (!EmailConfig.INSTANCE.enableWebVerification) return Optional.empty();

        String publicUrl = EmailConfig.INSTANCE.webVerificationPublicUrl;
        if (publicUrl == null || publicUrl.isBlank()) {
            AlixCommonMain.logWarning("'enable-web-verification' is on, but 'web-verification-public-url' is not set in email-config.yml! No verification link will be included in outgoing emails.");
            return Optional.empty();
        }

        String token = generateToken();
        TOKENS.put(token, pending);

        String base = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
        return Optional.of(base + "/verify?token=" + token);
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static final class RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            try {
                QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

                //FUNCTIONALITY (audit, 2026-09-24): restrict routing to the literal /verify path - previously
                //ANY path (e.g. /favicon.ico?token=..., /robots.txt?token=...) was treated identically, so
                //anything that happened to GET a URL carrying the token (a browser's own favicon fetch, a
                //crawler) could consume it.
                if (!"/verify".equals(decoder.path())) {
                    respond(ctx, HttpResponseStatus.NOT_FOUND, page(EmailConfig.INSTANCE.webVerificationPageInvalidTitle, EmailConfig.INSTANCE.webVerificationPageInvalidMessage, false));
                    return;
                }

                List<String> tokenParam = decoder.parameters().get("token");
                String token = tokenParam == null || tokenParam.isEmpty() ? null : tokenParam.get(0);

                //FUNCTIONALITY (audit, 2026-09-24): GET is no longer the step that actually consumes the
                //token/completes verification - it only PEEKS (asMap().get(), not remove()) and renders a
                //confirmation page with a real POST form. GET is supposed to be side-effect-free (RFC 7231
                //§4.2.1), and in practice corporate mail-gateway link scanners (Microsoft Defender Safe
                //Links, Proofpoint, Mimecast, etc.) and link-preview/unfurl bots automatically GET every URL
                //found in an email BEFORE a human ever opens it - which, under the old single-step GET
                //design, could silently burn the player's one-time token (denying their real click) or, for
                //a pending-registration link, complete their registration on their behalf without them ever
                //actually clicking anything. The POST below (submitted only by an actual browser following
                //the confirm button, which scanners don't do) is what does the real work now.
                if (request.method() == HttpMethod.GET) {
                    PendingWebVerification pending = token != null ? TOKENS.getIfPresent(token) : null;
                    if (pending == null) {
                        respond(ctx, HttpResponseStatus.BAD_REQUEST, page(EmailConfig.INSTANCE.webVerificationPageInvalidTitle, EmailConfig.INSTANCE.webVerificationPageInvalidMessage, false));
                        return;
                    }
                    respond(ctx, HttpResponseStatus.OK, confirmPage(token));
                    return;
                }

                if (request.method() != HttpMethod.POST) {
                    respond(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, page(EmailConfig.INSTANCE.webVerificationPageMethodNotAllowedTitle, EmailConfig.INSTANCE.webVerificationPageMethodNotAllowedMessage, false));
                    return;
                }

                //single-use: the token is removed as soon as it's looked up, regardless of outcome
                PendingWebVerification pending = token != null ? TOKENS.asMap().remove(token) : null;

                if (pending == null) {
                    respond(ctx, HttpResponseStatus.BAD_REQUEST, page(EmailConfig.INSTANCE.webVerificationPageInvalidTitle, EmailConfig.INSTANCE.webVerificationPageInvalidMessage, false));
                    return;
                }

                //Pending-registration case (see EmailHandler#sendVerifyMailForPendingRegistration) - there's no
                //account to look up yet, so the outcome is whatever onVerified() itself determines (gated behind
                //the same single-use verification code the in-game "/verifyemail <code>" path consumes).
                if (pending.onVerified() != null) {
                    if (pending.onVerified().getAsBoolean())
                        respond(ctx, HttpResponseStatus.OK, page(EmailConfig.INSTANCE.webVerificationPageSuccessTitle, EmailConfig.INSTANCE.webVerificationPageSuccessMessage, true));
                    else
                        respond(ctx, HttpResponseStatus.BAD_REQUEST, page(EmailConfig.INSTANCE.webVerificationPageInvalidTitle, EmailConfig.INSTANCE.webVerificationPageInvalidMessage, false));
                    return;
                }

                PersistentUserData data = UserFileManager.get(pending.playerName());
                if (data == null || !data.setEmail(pending.email())) {
                    respond(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, page(EmailConfig.INSTANCE.webVerificationPageErrorTitle, EmailConfig.INSTANCE.webVerificationPageErrorNotFoundMessage, false));
                    return;
                }

                respond(ctx, HttpResponseStatus.OK, page(EmailConfig.INSTANCE.webVerificationPageSuccessTitle, EmailConfig.INSTANCE.webVerificationPageSuccessMessage, true));
            } catch (Exception e) {
                AlixCommonMain.logWarning("Error handling a web verification request: " + e.getMessage());
                respond(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, page(EmailConfig.INSTANCE.webVerificationPageErrorTitle, EmailConfig.INSTANCE.webVerificationPageErrorGenericMessage, false));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            AlixCommonMain.logWarning("Error handling a web verification connection: " + cause.getMessage());
            ctx.close();
        }

        private void respond(ChannelHandlerContext ctx, HttpResponseStatus status, String html) {
            var content = Unpooled.copiedBuffer(html, CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String page(String title, String message, boolean success) {
        String color = success ? "#2e7d32" : "#c62828";
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>" + escape(title) + "</title>"
               + "<style>body{font-family:sans-serif;background:#111;color:#eee;display:flex;align-items:center;justify-content:center;height:100vh;margin:0}"
               + ".box{text-align:center;padding:2rem;max-width:32rem}h1{color:" + color + "}</style></head>"
               + "<body><div class=\"box\"><h1>" + escape(title) + "</h1><p>" + escape(message) + "</p></div></body></html>";
    }

    //The GET-time confirmation page - a real HTML form the human has to actually submit (a plain link-
    //scanner bot GET-ing this page never does), whose POST is what actually consumes the token and performs
    //the real action. token is our own SecureRandom-generated Base64 (URL-safe alphabet only, per
    //generateToken()), so it never needs HTML-escaping, but escape() is applied anyway as defense in depth
    //in case that ever changes.
    private static String confirmPage(String token) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>" + escape(EmailConfig.INSTANCE.webVerificationPageConfirmTitle) + "</title>"
               + "<style>body{font-family:sans-serif;background:#111;color:#eee;display:flex;align-items:center;justify-content:center;height:100vh;margin:0}"
               + ".box{text-align:center;padding:2rem;max-width:32rem}"
               + "button{font:inherit;padding:0.75rem 2rem;border:0;border-radius:0.5rem;background:#2e7d32;color:#eee;cursor:pointer}</style></head>"
               + "<body><div class=\"box\"><h1>" + escape(EmailConfig.INSTANCE.webVerificationPageConfirmTitle) + "</h1>"
               + "<p>" + escape(EmailConfig.INSTANCE.webVerificationPageConfirmMessage) + "</p>"
               + "<form method=\"POST\" action=\"/verify?token=" + escape(token) + "\">"
               + "<button type=\"submit\">" + escape(EmailConfig.INSTANCE.webVerificationPageConfirmButton) + "</button>"
               + "</form></div></body></html>";
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    //playerName/email are set (and onVerified is null) for the "attach email to an existing account" case;
    //onVerified is set (and playerName is null) for the "complete a pending registration" case - see the two
    //createVerificationLink() overloads above.
    private record PendingWebVerification(String playerName, String email, BooleanSupplier onVerified) {
    }
}
