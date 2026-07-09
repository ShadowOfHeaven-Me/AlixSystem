package alix.common.antibot.epoll.syn;

import alix.common.antibot.epoll.syn.signature.SynSignature;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

final class SynJ22Impl implements SynReaderWriter {

    private static final MethodHandle GETSOCKOPT;
    private static final MethodHandle SETSOCKOPT;

    static {
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();

        GETSOCKOPT = linker.downcallHandle(
                stdlib.find("getsockopt").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        );

        SETSOCKOPT = linker.downcallHandle(
                stdlib.find("setsockopt").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );
    }

    @Override
    public int enableSynSaving0(int serverFd) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment optval = arena.allocate(ValueLayout.JAVA_INT, 1);
            int result = (int) SETSOCKOPT.invokeExact(serverFd, IPPROTO_TCP, TCP_SAVE_SYN, optval, 4);
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    @Override
    public SynSignature parseSynSignature(int fd) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment optval = arena.allocate(256);
            MemorySegment optlen = arena.allocate(ValueLayout.JAVA_INT, (int) optval.byteSize());

            int result = (int) GETSOCKOPT.invokeExact(fd, IPPROTO_TCP, TCP_SAVED_SYN, optval, optlen);

            if (result == 0) {
                int len = optlen.get(ValueLayout.JAVA_INT, 0);
                if (len > 0) {
                    return SynReaderWriter.parseSignature(optval.asSlice(0, len).toArray(ValueLayout.JAVA_BYTE));
                }
            }
        } catch (Throwable t) {
            // FFM call failed (likely TCP_SAVE_SYN not set on server)
        }
        return null;
    }
}