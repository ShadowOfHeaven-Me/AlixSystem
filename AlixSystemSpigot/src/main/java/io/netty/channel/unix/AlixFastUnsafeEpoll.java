package io.netty.channel.unix;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.objectweb.asm.*;

import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;

public final class AlixFastUnsafeEpoll {

    public static void init() {
        //AlixUnsafe.getUnsafe().ensureClassInitialized(alixEpollConnection);

            /*for (Field f : Errors.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == int.class) {
                    Main.logError("FIELD: " + f.getName() + " VALUE: " + f.get(null));
                }
            }*/
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
                l.ensureInitialized(l.defineClass(in.readAllBytes()));
                //AlixUnsafe.getUnsafe().ensureClassInitialized(l.defineClass(in.readAllBytes()));
            }
            instrumentation.redefineClasses(new ClassDefinition(Socket.class, cw.toByteArray()));
        } catch (LinkageError ignored) {
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}