package shadow.utils.holders.packet.access;

import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;


final class Ver17To21AccessImpl implements VersionAccess {

    @Override
    public Object newGameStateGameModePacketInstance(int id) {
        return new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, id);
    }

    @Override
    public Object playerChatToSystemPacket(Object p) {
        ClientboundPlayerChatPacket c = (ClientboundPlayerChatPacket) p;
        return new ClientboundSystemChatPacket(c.g(), false);
    }
}