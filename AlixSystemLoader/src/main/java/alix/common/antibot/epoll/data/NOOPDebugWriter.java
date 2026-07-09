package alix.common.antibot.epoll.data;

import alix.common.antibot.epoll.TelemetryProfilerImpl;

final class NOOPDebugWriter implements DebugWriter {

    @Override
    public void writeToDisk(TelemetryProfilerImpl.ConnectionRecord record) {
    }
}