package ua.nanit.limbo.connection.captcha.blocks;

import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.blocks.PacketPlayOutBlockUpdate;

public final class CaptchaBlocks {

    public static final PacketSnapshot DECOY = of(new Vector3i(0, 62, 0), StateTypes.STONE);

    private static PacketSnapshot of(Vector3i pos, StateType type) {
        return PacketSnapshot.of(new PacketPlayOutBlockUpdate().setPosition(pos).setType(type));
    }

}