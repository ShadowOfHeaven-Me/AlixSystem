package ua.nanit.limbo.connection.pipeline.flush;

import io.netty.channel.Channel;

public interface FlushBatcher {

    void readBegin();

    void readComplete();

    void flush();

    boolean isEnabled = true;

    static FlushBatcher implFor(Channel channel) {
        return isEnabled ? new FlushBatcherImpl(channel) : new FlushBatcherNoBatching(channel);
    }

    static void flush0(Channel channel) {
        if (!channel.isActive()) return;
        channel.unsafe().flush();
    }
}