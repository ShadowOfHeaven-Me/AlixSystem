package alix.common.antibot.epoll;

import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollSocketChannel;

import static alix.common.antibot.epoll.Telemetry.ENABLED;

public final class TelemetryProfiler {

    public static final TelemetryProfiler PROFILER = new TelemetryProfiler();
    static final AbstractTelemetryProfiler IMPL = null;//TelemetryProfilerImpl.PROFILER;

    public static void enableSynSaving(int serverFd) {
        if (ENABLED)
            IMPL.enableSynSaving0(serverFd);
    }

    public void onConnection(int clientFd, byte[] addr) {
        if (ENABLED)
            IMPL.onConnection(clientFd, addr);
    }

    int clientFd(Channel channel) {
        return ((EpollSocketChannel) channel).fd().intValue();
    }

    public void onLoginStart(Channel channel) {
        if (ENABLED)
            IMPL.onLoginStart(this.clientFd(channel));
    }

    public void onStatusRequest(Channel channel) {
        if (ENABLED)
            IMPL.onStatusRequest(this.clientFd(channel));
    }

    public void onHandshake(Channel channel, int nextState) {
        if (ENABLED)
            IMPL.onHandshake(this.clientFd(channel), nextState);
    }

    public void removeClosed(Channel channel) {
        if (ENABLED)
            IMPL.removeClosed(this.clientFd(channel));
    }
}