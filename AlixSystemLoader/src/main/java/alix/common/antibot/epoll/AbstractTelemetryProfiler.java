package alix.common.antibot.epoll;

interface AbstractTelemetryProfiler {

    void enableSynSaving0(int serverFd);

    void onConnection(int clientFd, byte[] addr);

    void onHandshake(int clientFd, int nextState);

    void onLoginStart(int clientFd);

    void onStatusRequest(int clientFd);

    void removeClosed(int clientFd);

}