package alix.common.connection.profiler;


import io.netty.channel.Channel;
import ua.nanit.limbo.server.Log;

public final class LimboJoinProfiler {

    public static boolean PROFILE_JOINS = false;

    public static void update(Channel channel, ConnectionStage stage) {
        if (!PROFILE_JOINS)
            return;

        update(channel, stage, null);
    }

    public static void update(Channel channel, ConnectionStage stage, String extraInfo) {
        if (!PROFILE_JOINS)
            return;

        String appendExtra = extraInfo != null ? " extra: " + extraInfo : "";
        Log.info("Profiler: channel=" + channel.id() + " stage=" + stage + appendExtra);
    }
}