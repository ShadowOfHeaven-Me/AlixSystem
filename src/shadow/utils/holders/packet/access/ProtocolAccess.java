package shadow.utils.holders.packet.access;

import org.bukkit.GameMode;
import shadow.utils.holders.ReflectionUtils;

public final class ProtocolAccess {

    private static final VersionAccess impl = ReflectionUtils.protocolVersion ? new Ver17To21AccessImpl() : null;

    public static Object newGameModePacket(GameMode mode) {
        return impl.newGameStateGameModePacketInstance(mode.getValue());
    }

    public static Object convertPlayerChatToSystemPacket(Object packet) {
        return impl.playerChatToSystemPacket(packet);
    }

    private ProtocolAccess() {
    }
}