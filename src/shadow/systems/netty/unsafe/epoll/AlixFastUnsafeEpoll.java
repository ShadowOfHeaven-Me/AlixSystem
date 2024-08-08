/*
package shadow.systems.netty.unsafe.epoll;

import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.unix.FileDescriptor;
import io.netty.channel.unix.Socket;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import shadow.utils.main.AlixHandler;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public final class AlixFastUnsafeEpoll {

    //Source code: https://github.com/Jishuna/BetterPistons/blob/master/betterpistons-v1_20_R1/src/main/java/me/jishuna/betterpistons/nms/v1_20_R1/writer/PistonBaseClassWriter.java

    //Special thanks to mE-Shuggah (0x150)
    public static void init() {

        try {
            //Class.forName("io.netty.channel.unix.AlixEpollConnection", true, Socket.class.getClassLoader());
            Instrumentation instrumentation = ByteBuddyAgent.install();

            Class<MethodHandles.Lookup> lkCl = MethodHandles.Lookup.class;
            instrumentation.redefineModule(lkCl.getModule(),
                    Set.of(),
                    Map.of(),
                    Map.of("java.lang.invoke", Set.of(AlixFastUnsafeEpoll.class.getModule())),
                    Set.of(),
                    Map.of());

            Field lookupField = lkCl.getDeclaredField("IMPL_LOOKUP");
            lookupField.setAccessible(true);
            MethodHandles.Lookup lk = (MethodHandles.Lookup) lookupField.get(null);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "io/netty/channel/unix/AlixUnixBridge", null, "java/lang/Object", null);
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "HANDLE", "Ljava/lang/invoke/MethodHandle;", null, null).visitEnd();

            Class<?> bridgeClass = lk.defineClass(cw.toByteArray());
            Field test = bridgeClass.getDeclaredField("HANDLE");
            test.set(null, MethodHandles.lookup().findStatic(Class.forName("io.netty.channel.unix.AlixEpollConnection"), "handle",
                    MethodType.methodType(boolean.class, int.class, byte[].class)));

            instrumentation.addTransformer(new SocketClassTransformer(), true);
            instrumentation.retransformClasses(Socket.class);

*/
/*            JarOutputStream stream = new JarOutputStream(new ByteArrayOutputStream());
            stream.putNextEntry(new JarEntry("shadow/systems/netty/unsafe/epoll/AlixEpollConnection"));

            InputStream resource = Main.plugin.getResource("shadow/systems/netty/unsafe/epoll/AlixEpollConnection");

            byte[] buffer = new byte[8192];
            int read;
            while ((read = resource.read(buffer)) != -1) {
                stream.write(buffer, 0, read);
            }*//*


            //AlixFastUnsafeEpoll.class.getResource("AlixEpollConnection.class")

            */
/*MethodHandle handle = MethodHandles.lookup().findStatic(AlixEpollConnection.class,"reject", MethodType.methodType(boolean.class, int.class, byte[].class));

            Field methodHandleField = Socket.class.getField("EEEE");
            methodHandleField.set(null, handle);*//*


            EpollServerSocketChannel serverChannel = (EpollServerSocketChannel) AlixHandler.SERVER_CHANNEL_FUTURE.channel();

            Class<?> abstractEpollChannelClazz = Class.forName("io.netty.channel.epoll.AbstractEpollChannel");

            Field linuxServerSocketField = abstractEpollChannelClazz.getDeclaredField("socket");
            linuxServerSocketField.setAccessible(true);

            Socket serverOriginalSocket = (Socket) linuxServerSocketField.get(serverChannel);
            int fd = serverOriginalSocket.intValue();

            Class<?> linuxSocketClazz = Class.forName("io.netty.channel.epoll.LinuxSocket");
            Constructor<?> cons = linuxSocketClazz.getDeclaredConstructor(int.class);
            cons.setAccessible(true);
            Socket reloadedSocketClazz = (Socket) cons.newInstance(fd);

            Field fdStateField = FileDescriptor.class.getDeclaredField("state");
            fdStateField.setAccessible(true);

            fdStateField.set(reloadedSocketClazz, fdStateField.get(serverOriginalSocket));
            linuxServerSocketField.set(serverChannel, reloadedSocketClazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}*/
