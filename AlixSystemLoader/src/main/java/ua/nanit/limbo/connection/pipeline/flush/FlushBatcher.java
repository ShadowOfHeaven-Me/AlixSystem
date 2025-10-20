package ua.nanit.limbo.connection.pipeline.flush;

import io.netty.channel.Channel;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.server.Log;

public interface FlushBatcher {

    void readBegin();

    void readComplete();

    void flush();

    boolean isEnabled = true;

    static FlushBatcher implFor(Channel channel) {
        return isEnabled ? new FlushBatcherImpl(channel) : new FlushBatcherNoBatching(channel);
    }

    static void flush0(Channel channel) {
        channel.unsafe().flush();
        if (NanoLimbo.debugPackets)
            Log.warning("FLUSH0");
    }
}