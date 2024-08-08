//package shadow.systems.netty.unsafe.epoll;
//
//
//import org.objectweb.asm.*;
//import org.objectweb.asm.commons.AdviceAdapter;
//import shadow.Main;
//
//import java.lang.instrument.ClassFileTransformer;
//import java.security.ProtectionDomain;
//
//public final class SocketClassTransformer implements ClassFileTransformer {
//    private static final String TARGET_CLASS = "io/netty/channel/unix/Socket";
//    private static final String TARGET_METHOD = "accept";
//    private static final String TARGET_METHOD_DESC = "([B)I";
//
//    @Override
//    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classDef) {
//        Main.logError("CLAZZZ " + className);
//        if (!className.equals(TARGET_CLASS)) {
//            return null;
//        }
//
//        ClassReader cr = new ClassReader(classDef);
//        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
//        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
//
//
//            /*@Override
//            public void visitEnd() {
//                FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "EEEE", "Ljava/lang/invoke/MethodHandle;", null, null);
//                if (fv != null) {
//                    fv.visitEnd();
//                }
//                super.visitEnd();
//            }*/
//
//            @Override
//            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
//                if (name.equals(TARGET_METHOD) && desc.equals(TARGET_METHOD_DESC)) {
//                    return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
//
//                        @Override
//                        public void visitVarInsn(int opcode, int varIndex) {
//                            super.visitVarInsn(opcode, varIndex);
//                            if (opcode == Opcodes.ISTORE && varIndex == 2) {
//                                mv.visitFieldInsn(Opcodes.GETSTATIC, "java.lang.invoke.AlixUnixBridge", "HANDLE", "Ljava/lang/invoke/MethodHandle;");
//                                mv.visitVarInsn(Opcodes.ILOAD, 2); // Load 'res'
//                                mv.visitVarInsn(Opcodes.ALOAD, 1); // Load 'addr'
//                                /*mv.visitMethodInsn(Opcodes.INVOKESTATIC,
//                                        "shadow/systems/netty/unsafe/epoll/AlixEpollConnection",
//                                        "reject",
//                                        "(I[B)Z",
//                                        false);*/
//                                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", "(I[B)Z", false);
//
//                                Label l1 = new Label();
//                                mv.visitJumpInsn(Opcodes.IFEQ, l1);
//                                mv.visitInsn(Opcodes.ICONST_M1);
//                                mv.visitInsn(Opcodes.IRETURN);
//                                mv.visitLabel(l1);
//                            }
//                        }
//                    };
//                }
//                return mv;
//            }
//        };
//
//        cr.accept(cv, ClassReader.EXPAND_FRAMES);
//        return cw.toByteArray();
//    }
//}