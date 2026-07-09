package alix.common.antibot.epoll.syn;

import alix.common.antibot.epoll.syn.signature.SynSignature;
import alix.common.reflection.CommonReflection;
import io.netty.channel.unix.Socket;

import java.lang.reflect.Method;

final class NettySynImpl implements SynReaderWriter {

    private static final Method setIntOpt = CommonReflection.getDeclaredMethodAccessible(Socket.class, "setIntOpt",
            int.class, int.class, int.class, int.class);
    private static final Method getRawOptArray = CommonReflection.getDeclaredMethodAccessible(Socket.class, "getRawOptArray",
            int.class, int.class, int.class, byte[].class, int.class, int.class);

    @Override
    public int enableSynSaving0(int serverFd) {
        try {
            setIntOpt.invoke(null, serverFd, IPPROTO_TCP, TCP_SAVE_SYN, 1);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static final ThreadLocal<byte[]> CACHE = ThreadLocal.withInitial(() -> new byte[256]);

    @Override
    public SynSignature parseSynSignature(int fd) {
        byte[] out = CACHE.get();

        try {
            getRawOptArray.invoke(null, fd, IPPROTO_TCP, TCP_SAVED_SYN, out, 0, out.length);
            return SynReaderWriter.parseSignature(out);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}