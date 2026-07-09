package alix.common.antibot.epoll.data;

import alix.common.antibot.epoll.TelemetryProfilerImpl;

public interface DebugWriter {

    void writeToDisk(TelemetryProfilerImpl.ConnectionRecord record);

    static DebugWriter newImpl(boolean noop) {
        return noop ? new NOOPDebugWriter() : new FileOutputDebugWriterImpl();
    }
}