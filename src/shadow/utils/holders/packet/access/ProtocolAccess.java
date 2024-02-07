package shadow.utils.holders.packet.access;

import org.bukkit.GameMode;

public final class ProtocolAccess {

    private static final VersionAccess impl = new Ver17To21AccessImpl();

    public static Object newGameModePacket(GameMode mode) {
        return impl.newGameStateGameModePacketInstance(mode.getValue());
    }

    private ProtocolAccess() {
    }
}