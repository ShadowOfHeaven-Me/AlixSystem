package io.netty.channel.unix;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.objectweb.asm.*;

import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class AlixFastUnsafeEpoll {

    //Special thanks to xDark for providing me with this class ;]

    //we need to pass AlixEpollConnection.class from the outside, cuz class loader shenanigans don't see it from this class
    public static void init(Class<?> alixEpollConnectionClazz) {
        try {
            Instrumentation instrumentation = ByteBuddyAgent.install();
            ClassLoader cl = Socket.class.getClassLoader();
            ClassReader cr;
            try (InputStream in = cl.getResourceAsStream(Socket.class.getName().replace('.', '/') + ".class")) {
                cr = new ClassReader(in.readAllBytes());
            }
            ClassWriter cw = new ClassWriter(cr, 0);
            cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                    if (!"accept".equals(name))
                        return mv;
                    if (!"([B)I".equals(descriptor))
                        return mv;
                    return new MethodVisitor(Opcodes.ASM9, mv) {

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            check:
                            {
                                if (opcode != Opcodes.INVOKESTATIC)
                                    break check;
                                if (!"io/netty/channel/unix/Socket".equals(owner))
                                    break check;
                                if (!"accept".equals(name))
                                    break check;
                                if (!"(I[B)I".equals(descriptor))
                                    break check;
                                owner = Type.getInternalName(AlixSocketAccessBridge.class);
                                name = "acceptBridge";
                            }
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    };
                }
            }, 0);
            try (InputStream in = AlixFastUnsafeEpoll.class.getClassLoader().getResourceAsStream("io/netty/channel/unix/AlixSocketAccessBridge.class")) {

                MethodHandles.Lookup l = MethodHandles.privateLookupIn(Socket.class, MethodHandles.lookup());
                Class<?> bridgeClass = l.defineClass(in.readAllBytes());
                l.ensureInitialized(bridgeClass);

                MethodHandle pluginTargetHandle = MethodHandles.lookup().findStatic(alixEpollConnectionClazz, "handle",
                        MethodType.methodType(int.class, int.class, byte[].class));

                MethodHandle initDelegateHandle = MethodHandles.lookup().findStatic(bridgeClass, "initDelegate",
                        MethodType.methodType(void.class, MethodHandle.class));

                initDelegateHandle.invokeExact(pluginTargetHandle);
                //AlixSocketAccessBridge.initDelegate(pluginTargetHandle);
            }
            instrumentation.redefineClasses(new ClassDefinition(Socket.class, cw.toByteArray()));
        } /*catch (LinkageError ex) {
            ex.printStackTrace();
            throw ex;
        } */ catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}