package shadow.utils.objects.packet.types.unverified;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;

public final class BedrockPacketBlocker extends PacketBlocker {

    BedrockPacketBlocker(PacketBlocker previousBlocker) {
        super(previousBlocker);
    }

    BedrockPacketBlocker(UnverifiedUser u, TemporaryUser t) {
        super(u, t);
    }

    @Override
    void onReceive0(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                this.virtualFallPhase.trySpoofPackets(event);
                break;
            case TELEPORT_CONFIRM:
                this.virtualFallPhase.tpConfirm(event);
                break;
            case PLUGIN_MESSAGE:
            case CLOSE_WINDOW:
            case PONG:
            case KEEP_ALIVE://will time out without this one
                return;
        }
        event.setCancelled(true);
    }
}