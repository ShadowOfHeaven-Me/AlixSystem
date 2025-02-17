package ua.nanit.limbo.connection.pipeline.flush;

import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

final class FlushBatcherImpl implements FlushBatcher {

    //Strongly inspired by FlushConsolidationHandler

    private final Channel channel;
    private boolean pendingFlush, isInRead;
    private ScheduledFuture<?> flushTask;

    FlushBatcherImpl(Channel channel) {
        this.channel = channel;
    }

    private void flush0() {
        FlushBatcher.flush0(this.channel);
        this.flushTask = null;
    }

    private void tryFlush() {
        if (this.flushTask != null) return;
        this.flushTask = this.channel.eventLoop().schedule(this::flush0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void readBegin() {
        this.isInRead = true;
    }

    @Override
    public void readComplete() {
        this.isInRead = false;
        if (!this.pendingFlush) return;

        this.tryFlush();
        this.pendingFlush = false;
    }

    private void assertEventLoop() {
        if (!this.channel.eventLoop().inEventLoop())
            throw new AlixException("Not in eventLoop!");
    }

    @Override
    public void flush() {
        this.assertEventLoop();

        if (this.isInRead) this.pendingFlush = true;
        else this.tryFlush();
    }
}