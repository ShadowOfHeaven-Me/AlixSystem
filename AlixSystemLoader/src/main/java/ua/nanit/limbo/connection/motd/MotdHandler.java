package ua.nanit.limbo.connection.motd;

import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.environment.ServerEnvironment;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.netty.BufRelease;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import io.netty.buffer.ByteBuf;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.status.PacketStatusPong;
import ua.nanit.limbo.protocol.packets.status.PacketStatusResponse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class MotdHandler {

    //Status Response
    private static final long CACHE_WINDOW_MS = 6_000L;
    private static final long DEFERRED_RELEASE_DELAY_MS = 10_000;
    private static final boolean ENABLED = ServerEnvironment.isVelocity() && ConfigProvider.config.getBoolean("optimize-pings");
    private static final AtomicBoolean ENCODING = new AtomicBoolean();
    private static final AtomicLong NEXT_ENCODE = new AtomicLong();
    private static volatile ByteBuf CACHED_CONST_RESP;

    //Pong
    private static final ByteBuf PONG = PacketUtils.constClientbound(new PacketStatusPong());

    public static void sendPong(ClientConnection connection) {
        connection.write(PONG);
        connection.getChannel().unsafe().flush();
        //connection.flush();

        connection.close();
    }

    public static boolean sendCachedResponse(ClientConnection connection) {
        if (!ENABLED || isExpired()) return false;

        var channel = connection.getChannel();
        LimboJoinProfiler.update(channel, ConnectionStage.OPTIMIZED_STATUS_RESPONSE);

        //AlixCommonMain.logError("sendCachedResponse");

        connection.write(CACHED_CONST_RESP);
        connection.write(PONG);
        channel.unsafe().flush();

        connection.close();

        connection.replyingWithCachedMotd = true;
        return true;
    }

    static boolean isExpired() {
        return System.currentTimeMillis() > NEXT_ENCODE.get();
    }

    public static void feed(PacketSendEvent event) {
        if (!ENABLED || !isExpired()) return;

        if (!ENCODING.compareAndSet(false, true)) return;

        if (!isExpired()) {
            ENCODING.set(false);
            return;
        }

        var wrapper = new WrapperStatusServerResponse(event);
        var limbo = new PacketStatusResponse(wrapper.getComponentJson());

        //make sure the dealloc does not happen to the recent buffer
        var prev = CACHED_CONST_RESP;
        AlixScheduler.runLaterAsync(() -> {
            //null-safe
            BufRelease.releaseConst(prev);
        }, DEFERRED_RELEASE_DELAY_MS, TimeUnit.MILLISECONDS);

        CACHED_CONST_RESP = PacketUtils.constClientbound(limbo);

        ENCODING.set(false);
        NEXT_ENCODE.set(System.currentTimeMillis() + CACHE_WINDOW_MS);
    }
}