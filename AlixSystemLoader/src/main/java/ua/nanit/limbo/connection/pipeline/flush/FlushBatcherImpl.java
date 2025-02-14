package ua.nanit.limbo.connection.pipeline.flush;

import io.netty.channel.Channel;

final class FlushBatcherImpl implements FlushBatcher {

    private final Channel channel;
    private boolean pendingFlush, isInRead;

    FlushBatcherImpl(Channel channel) {
        this.channel = channel;
    }

    private void flush0() {
        FlushBatcher.flush0(this.channel);
    }

    @Override
    public void readBegin() {
        this.isInRead = true;
    }

    @Override
    public void readComplete() {
        this.isInRead = false;
        if (!this.pendingFlush) return;

        this.flush0();
        this.pendingFlush = false;
    }

    @Override
    public void flush() {
        if (this.isInRead) this.pendingFlush = true;
        else this.flush0();
    }
}