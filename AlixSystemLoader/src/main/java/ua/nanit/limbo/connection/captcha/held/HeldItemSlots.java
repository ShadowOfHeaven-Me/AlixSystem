package ua.nanit.limbo.connection.captcha.held;

import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.slot.PacketPlayOutHeldSlot;

public final class HeldItemSlots {

    public static final PacketSnapshot INVALID = ofId(-1);
    public static final int VALID_SLOT = 1;
    public static final PacketSnapshot VALID = ofId(VALID_SLOT);

    private static PacketSnapshot ofId(int slot) {
        return PacketSnapshot.of(new PacketPlayOutHeldSlot().setSlot(slot));
    }
}