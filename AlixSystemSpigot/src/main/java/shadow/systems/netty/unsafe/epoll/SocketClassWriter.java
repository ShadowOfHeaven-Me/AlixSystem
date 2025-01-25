/*
package shadow.systems.netty.unsafe.epoll;

import io.netty.channel.unix.Socket;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class SocketClassWriter {

    //https://repository.ow2.org/nexus/content/repositories/releases/org/ow2/asm/

    private final ClassReader reader;
    private final ClassWriter writer;

    public SocketClassWriter(byte[] classDef) {
        this.reader = new ClassReader(classDef);
        this.writer = new ClassWriter(this.reader, ClassWriter.COMPUTE_FRAMES);
    }

    public byte[] rewrite() {
        ClassNode node = new ClassNode();
        this.reader.accept(node, 0);
        for (MethodNode method : node.methods) {
            Type type = Type.getType(method.desc);
            if(type == Type.INT_TYPE && method.name.equals("accept") && method.parameters != null && method.parameters.size() == 1 && method.parameters.get(0).) {

            }
        }
    }

}*/
