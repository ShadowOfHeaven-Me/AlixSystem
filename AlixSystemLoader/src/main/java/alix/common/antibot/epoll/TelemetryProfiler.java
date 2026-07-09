package alix.common.antibot.epoll;

import alix.common.antibot.epoll.syn.signature.SynSignature;
import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollSocketChannel;

import static alix.common.antibot.epoll.Telemetry.ENABLED;

public final class TelemetryProfiler {

    public static final TelemetryProfiler PROFILER = new TelemetryProfiler();
    static final AbstractTelemetryProfiler IMPL = TelemetryProfilerImpl.PROFILER;

    public static void enableSynSaving(int serverFd) {
        if (ENABLED)
            IMPL.enableSynSaving(serverFd);
    }

    public static SynSignature synSignature(Channel channel) {
        if (!ENABLED)
            return null;

        var record = IMPL.record(clientFd(channel));
        return record != null ? record.synSignature : null;
    }

    public void onConnection(int clientFd, byte[] addr) {
        if (ENABLED)
            IMPL.onConnection(clientFd, addr);
    }

    public void onLoginStart(Channel channel) {
        if (ENABLED)
            IMPL.onLoginStart(clientFd(channel));
    }

    public void onStatusRequest(Channel channel) {
        if (ENABLED)
            IMPL.onStatusRequest(clientFd(channel));
    }

    public void onHandshake(Channel channel, int nextState) {
        if (ENABLED)
            IMPL.onHandshake(clientFd(channel), nextState);
    }

    public static void removeClosed(Channel channel) {
        if (ENABLED)
            IMPL.removeClosed(clientFd(channel));
    }

    static int clientFd(Channel channel) {
        return ((EpollSocketChannel) channel).fd().intValue();
    }
}