package shadow.utils.holders.packet.access;

import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;

final class Ver17To21AccessImpl implements VersionAccess {

    @Override
    public Object newGameStateGameModePacketInstance(int id) {
        return new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, id);
    }
}