package alix.common.data.fingerprinting;

import alix.common.AlixCommonMain;
import alix.common.data.fingerprinting.resourcepack.PrependedResponse;
import alix.common.data.fingerprinting.resourcepack.ResourcePackGenerator;
import alix.common.data.fingerprinting.resourcepack.file.VirtualResourcePack;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FingerprintHandler extends ChannelInboundHandlerAdapter {

    public static final Map<String, FingerprintSession> MAP = new ConcurrentHashMap<>();
    public static final String URL_PREFIX = "/fp/";

    private static final FullHttpResponse NOT_FOUND;
    static {
        NOT_FOUND = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        NOT_FOUND.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
    }

    private static final PrependedResponse[] RESPONSES = generateResponses();

    @SneakyThrows
    private static PrependedResponse[] generateResponses() {
        var res = new PrependedResponse[FingerprintBuilder.LEN];
        for (int i = 0; i < res.length; i++) {
            res[i] = new PrependedResponse(generateResponse(ResourcePackGenerator.generate(i), i), i);
        }
        return res;
    }

    private static FullHttpResponse generateResponse(VirtualResourcePack resourcePack, int bit) throws Exception {
        byte[] zipBytes = resourcePack.toByteArray();

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(zipBytes)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/zip");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, zipBytes.length);
        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION,
                "attachment; filename=\"alix-device-key-" + bit + ".zip\"");
        return response;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (!(msg instanceof FullHttpRequest request)) {
                AlixCommonMain.logError("Unknown message type " + msg);
                return;
            }

            String path;
            try {
                path = new URI(request.uri()).getPath();
            } catch (Exception e) {
                this.sendNotFound(ctx);
                return;
            }

            if (path == null || !path.startsWith(URL_PREFIX)) {
                this.sendNotFound(ctx);
                return;
            }

            int slashIdx = path.indexOf('/', URL_PREFIX.length());
            if (slashIdx == -1) {
                this.sendNotFound(ctx);
                return;
            }

            String name = path.substring(URL_PREFIX.length(), slashIdx).toLowerCase(Locale.ROOT);
            int bit;
            try {
                bit = Integer.parseInt(path.substring(slashIdx + 1));
            } catch (Exception e) {
                this.sendNotFound(ctx);
                return;
            }

            if (bit < 0 || bit >= RESPONSES.length) {
                this.sendNotFound(ctx);
                return;
            }

            var session = MAP.get(name);
            if (session == null) {
                this.sendNotFound(ctx);
                return;
            }

            session.markDownloadRequested(bit);
            if (session.isTestMode()) {
                this.sendNotFound(ctx);
                return;
            }

            var prepended = RESPONSES[bit];
            ctx.writeAndFlush(prepended.response().retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void sendNotFound(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(NOT_FOUND.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
    }
}
