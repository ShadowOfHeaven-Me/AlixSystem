package alix.common.antibot.epoll;

import alix.common.antibot.epoll.TelemetryProfilerImpl.ConnectionRecord;

interface AbstractTelemetryProfiler {

    void enableSynSaving(int serverFd);

    ConnectionRecord record(int clientFd);

    void onConnection(int clientFd, byte[] addr);

    void onHandshake(int clientFd, int nextState);

    void onLoginStart(int clientFd);

    void onStatusRequest(int clientFd);

    void removeClosed(int clientFd);

}