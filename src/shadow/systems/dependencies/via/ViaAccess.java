package shadow.systems.dependencies.via;


import alix.libs.com.github.retrooper.packetevents.PacketEvents;
import alix.libs.com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import alix.libs.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.packet.PacketWrapperImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import shadow.Main;

import java.util.List;
import java.util.stream.Collectors;

public final class ViaAccess {

    public static ByteBuf convert(ByteBuf packet, ClientVersion retrooperTargetVer) {
        int srcProtocolVer = PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion();
        ProtocolVersion srcVer = ProtocolVersion.getProtocol(srcProtocolVer);
        ProtocolVersion targetVer = ProtocolVersion.getProtocol(retrooperTargetVer.getProtocolVersion());
        return convertPacket(packet, srcVer, targetVer);
    }

    //Thanks EnZaXD (florianmichael) ^^
    private static ByteBuf convertPacket(ByteBuf packet, ProtocolVersion srcVersion, ProtocolVersion targetVersion) {
        List<ProtocolPathEntry> protocolPath = Via.getManager().getProtocolManager().getProtocolPath(srcVersion, targetVersion);
        if (protocolPath == null) {
            Main.logWarning("Failed to build protocol path: " + srcVersion + " " + targetVersion + "!");
            return null;
        }
        try {
            int packetId = ByteBufHelper.readVarInt(packet);
            PacketWrapperImpl wrapper = new PacketWrapperImpl(packetId, packet, new UserConnectionImpl(null, false));
            wrapper.apply(Direction.SERVERBOUND, State.PLAY, protocolPath.stream().map(ProtocolPathEntry::protocol).collect(Collectors.toList()));
            ByteBuf output = Unpooled.buffer();
            wrapper.writeToBuffer(output);
            //Types.VAR_INT.readPrimitive(output); // Remove packet id again
            return output;
        } catch (Exception e) {
            Main.logWarning("Failed to convert packet: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}