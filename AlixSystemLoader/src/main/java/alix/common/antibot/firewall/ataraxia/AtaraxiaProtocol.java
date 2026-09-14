package alix.common.antibot.firewall.ataraxia;

import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collection;

final class AtaraxiaProtocol {

    static final int PROTOCOL_VERSION = 0;

    private static final int
            J2R_HANDSHAKE = 0,
            J2R_UPDATE_MAP = 1;

    static final int
            R2J_HANDSHAKE_REPLY = 0,
            R2J_UPDATE_MAP = 1;


    static ByteBuf encodeJ2RHandshake() {
        ByteBuf buf = ServerHandler.buffer();

        buf.writeByte(J2R_HANDSHAKE);
        buf.writeByte(PROTOCOL_VERSION);

        return encodeLen(buf);
    }

    static ByteBuf encodeJ2RMapUpdate(boolean isBlacklist, boolean add, Collection<InetAddress> ips) {
        ByteBuf buf = ServerHandler.buffer();

        buf.writeByte(J2R_UPDATE_MAP);
        buf.writeBoolean(isBlacklist);
        buf.writeBoolean(add);
        buf.writeInt(ips.size());

        for (var addr : ips)
            writeAddr(buf, addr);

        return encodeLen(buf);
    }

    static void writeAddr(ByteBuf buf, InetAddress addr) {
        buf.writeBoolean(addr instanceof Inet6Address);
        buf.writeBytes(addr.getAddress());//4 for ipv4, 16 for ipv6
    }

    @SneakyThrows
    static InetAddress readAddr(ByteBuf buf) {
        boolean ipv6 = buf.readBoolean();
        byte[] bytes = new byte[ipv6 ? 16 : 4];

        buf.readBytes(bytes);

        return InetAddress.getByAddress(bytes);
    }

    private static ByteBuf encodeLen(ByteBuf buf) {
        int len = buf.writerIndex();

        ByteBuf encoded = ServerHandler.buffer();
        encoded.writeInt(len);
        encoded.writeBytes(buf);

        buf.release();

        return encoded;
    }
}