package alix.common.antibot.epoll.syn;

import alix.common.AlixCommonMain;
import alix.common.antibot.epoll.syn.signature.SynSignature;

final class SynNativeCImpl implements SynReaderWriter {

    static {
        try {
            // Load the native C library (e.g., libalix_epoll.so on Linux)
            System.loadLibrary("alix_epoll");
        } catch (UnsatisfiedLinkError e) {
            AlixCommonMain.logInfo("[Telemetry] Native library 'alix_epoll' not found. SYN saving functions will fail.");
        }
    }

    @Override
    public int enableSynSaving0(int serverFd) {
        return setTcpSaveSyn(serverFd);
    }

    @Override
    public SynSignature parseSynSignature(int fd) {
        return SynReaderWriter.parseSignature(getTcpSavedSyn(fd));
    }

    private static native int setTcpSaveSyn(int fd);
    private static native byte[] getTcpSavedSyn(int fd);
}