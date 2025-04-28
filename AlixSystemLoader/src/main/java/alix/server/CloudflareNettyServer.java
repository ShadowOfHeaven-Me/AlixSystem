package alix.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;


public final class CloudflareNettyServer {

    private static final String TURNSTILE_SECRET_KEY = "YOUR_SECRET_KEY"; // Replace with your actual secret key
    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
    private static final String SITE_KEY = "YOUR_SITE_KEY"; // Replace with your actual site key

    // Server configuration
    private static final int PORT = 8080;

    // Logger
    private static final Logger logger = Logger.getLogger(CloudflareNettyServer.class.getName());

    // Gson instance for JSON processing
    private static final Gson gson = new Gson();

    // HTTP client for Turnstile verification
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static void main(String[] args) throws Exception {
        // Configure the server
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerInitializer());

            // Start the server
            Channel ch = b.bind(PORT).sync().channel();
            logger.info("Server started at http://localhost:" + PORT + '/');

            // Wait until the server socket is closed
            ch.closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Initialize the server channel pipeline
     */
    static class ServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(65536));
            p.addLast(new ServerHandler());
        }
    }

    /**
     * Handle HTTP requests and process Turnstile validation
     */
    static class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
            // Determine what kind of request it is
            if (req.method() == HttpMethod.GET) {
                handleGetRequest(ctx, req);
            } else if (req.method() == HttpMethod.POST && req.uri().equals("/verify")) {
                handleTurnstileVerification(ctx, req);
            } else {
                sendResponse(ctx, HttpResponseStatus.NOT_FOUND, "Not Found");
            }
        }

        /**
         * Handle GET requests by serving the form with Turnstile integration
         */
        private void handleGetRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
            // Serve the HTML form with Turnstile widget
            String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Cloudflare Turnstile Demo (Gson)</title>\n" +
                    "    <script src=\"https://challenges.cloudflare.com/turnstile/v0/api.js\" async defer></script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <h1>Cloudflare Turnstile Demo with Gson</h1>\n" +
                    "    <form action=\"/verify\" method=\"POST\">\n" +
                    "        <div class=\"cf-turnstile\" data-sitekey=\"" + SITE_KEY + "\"></div>\n" +
                    "        <br>\n" +
                    "        <input type=\"submit\" value=\"Submit\">\n" +
                    "    </form>\n" +
                    "</body>\n" +
                    "</html>";

            sendResponse(ctx, HttpResponseStatus.OK, html);
        }

        /**
         * Handle Turnstile verification requests
         */
        private void handleTurnstileVerification(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
            // Parse form data from the POST request
            String content = req.content().toString(CharsetUtil.UTF_8);
            Map<String, String> formData = parseFormData(content);

            // Extract the Turnstile token
            String token = formData.get("cf-turnstile-response");

            if (token == null || token.isEmpty()) {
                sendResponse(ctx, HttpResponseStatus.BAD_REQUEST, "Missing Turnstile token");
                return;
            }

            // Verify the token with Cloudflare
            verifyTurnstileToken(token).thenAccept(result -> {
                try {
                    // Extract the success state and other data using Gson
                    boolean success = result.get("success").getAsBoolean();

                    // Safely extract other fields that might not be present
                    String hostname = getStringOrDefault(result, "hostname", "N/A");
                    String challengeTS = getStringOrDefault(result, "challenge_ts", "N/A");
                    String action = getStringOrDefault(result, "action", "N/A");
                    String cdata = getStringOrDefault(result, "cdata", "N/A");

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("<!DOCTYPE html>\n")
                            .append("<html><head><title>Verification Result</title></head><body>\n")
                            .append("<h1>Turnstile Verification Result (Gson)</h1>\n")
                            .append("<p><strong>Success:</strong> ").append(success).append("</p>\n")
                            .append("<p><strong>Hostname:</strong> ").append(hostname).append("</p>\n")
                            .append("<p><strong>Challenge Timestamp:</strong> ").append(challengeTS).append("</p>\n")
                            .append("<p><strong>Action:</strong> ").append(action).append("</p>\n")
                            .append("<p><strong>Customer Data:</strong> ").append(cdata).append("</p>\n");

                    // Show error codes if any
                    if (result.has("error-codes") && result.get("error-codes").isJsonArray()) {
                        responseBuilder.append("<p><strong>Error Codes:</strong> ");
                        JsonArray errors = result.get("error-codes").getAsJsonArray();
                        for (int i = 0; i < errors.size(); i++) {
                            responseBuilder.append(errors.get(i).getAsString());
                            if (i < errors.size() - 1) {
                                responseBuilder.append(", ");
                            }
                        }
                        responseBuilder.append("</p>\n");
                    }

                    responseBuilder.append("<p><a href=\"/\">Go Back</a></p>\n")
                            .append("</body></html>");

                    sendResponse(ctx, HttpResponseStatus.OK, responseBuilder.toString());
                } catch (Exception e) {
                    logger.warning("Error processing verification result: " + e.getMessage());
                    sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error processing verification result: " + e.getMessage());
                }
            }).exceptionally(e -> {
                logger.warning("Error verifying token: " + e.getMessage());
                sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error verifying token: " + e.getMessage());
                return null;
            });
        }

        /**
         * Helper method to safely extract a string from a JsonObject
         */
        private String getStringOrDefault(JsonObject json, String key, String defaultValue) {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
            return defaultValue;
        }

        /**
         * Verify a Turnstile token with Cloudflare's API
         */
        private CompletableFuture<JsonObject> verifyTurnstileToken(String token) {
            // Create the form data for the verification request
            String formData = "secret=" + TURNSTILE_SECRET_KEY + "&response=" + token;

            // Build the request to Cloudflare
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TURNSTILE_VERIFY_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            // Send the request and parse the response with Gson
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            // Parse the JSON response using Gson
                            return gson.fromJson(response.body(), JsonObject.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse verification response", e);
                        }
                    });
        }

        /**
         * Parse form data from a request body with proper URL decoding
         */
        private Map<String, String> parseFormData(String content) {
            Map<String, String> formData = new HashMap<>();
            String[] pairs = content.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    formData.put(key, value);
                }
            }
            return formData;
        }

        /**
         * Send an HTTP response back to the client
         */
        private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    status,
                    Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            // Send the response and close the connection if needed
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.warning("Exception caught: " + cause.getMessage());
            cause.printStackTrace();
            ctx.close();
        }
    }
}