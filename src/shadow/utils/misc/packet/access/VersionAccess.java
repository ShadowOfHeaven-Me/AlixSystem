package shadow.utils.misc.packet.access;

interface VersionAccess {

    Object newGameStateGameModePacketInstance(int id);

    Object playerChatToSystemPacket(Object p);

}