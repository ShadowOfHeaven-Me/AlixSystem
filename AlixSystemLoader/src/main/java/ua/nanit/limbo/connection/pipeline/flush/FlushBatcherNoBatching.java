package ua.nanit.limbo.connection.pipeline.flush;

import io.netty.channel.Channel;

final class FlushBatcherNoBatching implements FlushBatcher {

    private final Channel channel;

    FlushBatcherNoBatching(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void readBegin() {
    }

    @Override
    public void readComplete() {
    }

    @Override
    public void flush() {
        FlushBatcher.flush0(this.channel);
    }
}