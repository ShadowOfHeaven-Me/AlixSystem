package shadow.utils.objects.packet.types.unverified;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;

public final class AuthGUIPacketBlocker extends PacketBlocker {

    AuthGUIPacketBlocker(PacketBlocker previousBlocker) {
        super(previousBlocker);
    }

    AuthGUIPacketBlocker(UnverifiedUser u, TemporaryUser t) {
        super(u, t);
    }
    @Override
    void onReceive0(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                //Main.logError("LOC RECEIVE: " + new WrapperPlayClientPlayerFlying(event).getLocation());
                this.virtualFallPhase.trySpoofPackets(event);
                event.setCancelled(true);
                return;
            case TELEPORT_CONFIRM:
                this.virtualFallPhase.tpConfirm(event);
                break;
            case CLOSE_WINDOW:
                event.getPostTasks().add(user::openVerificationGUI);
                break;
            case CLICK_WINDOW://item spoofing happens here \/
                this.user.getVerificationGUI().select(new WrapperPlayClientClickWindow(event).getSlot());
                break;
            case KEEP_ALIVE://will time out without this one
                return;
        }
        event.setCancelled(true);
    }

    /*@Override
    public void onPacketSend(PacketPlaySendEvent event) {
        //if (spoofedWindowItems(msg)) return;
        if (!user.hasCompletedCaptcha()) {
            super.onSendCaptchaVerification(event);
            return;
        }
        //if (event.getPacketType() == WINDOW_ITEMS) return;

        super.onPacketSend(event);
    }*/

 /*   @Override
    public void onPacketSend(PacketPlaySendEvent event) {
    //if (spoofedWindowItems(msg)) return;
        if (user.hasCompletedCaptcha()) {
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayOutChat":
                case "ClientboundSystemChatPacket":
                case "ClientboundDisguisedChatPacket":
                case "PacketPlayOutTitle":
                case "ClientboundSetTitlesAnimationPacket":
                case "ClientboundSetTitleTextPacket":
                case "ClientboundSetSubtitleTextPacket":
                    this.blockedChatPackets.offerLast(msg);
                    return;
                case "ClientboundPlayerChatPacket":
                    this.blockedChatPackets.offerLast(ProtocolAccess.convertPlayerChatToSystemPacket(msg));
                    return;
                case "PacketPlayOutRespawn":
                case "ClientboundRespawnPacket":
                    this.waitPackets += WAIT_PACKETS_INCREASE;
                    break;
                //case "PacketPlayOutGameStateChange":
                //case "PacketPlayOutWindowItems":
                case "PacketPlayOutRelEntityMove":
                case "PacketPlayOutNamedEntitySpawn":
                case "PacketPlayOutSpawnEntityLiving":
                case "PacketPlayOutSpawnEntity":
                case "PacketPlayOutEntityEquipment":
                case "PacketPlayOutEntityHeadRotation":
                case "PacketPlayOutEntityVelocity":
                case "PacketPlayOutEntityDestroy":
                case "PacketPlayOutEntityLook":
                case "PacketPlayOutPlayerInfo":
                case "ClientboundPlayerInfoUpdatePacket":
                    //Main.logInfo("BLOCKED: " + msg.getClass().getSimpleName());
                    return;
            }
            //Main.logInfo("RECEIVED: " + msg.getClass().getSimpleName());
            return;
        }
        this.onWriteCaptchaVerification(ctx, msg, promise);
    }*/
}