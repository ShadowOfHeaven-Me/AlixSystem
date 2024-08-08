package shadow.utils.objects.packet.types.unverified;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import shadow.utils.users.types.UnverifiedUser;

public class GUIPacketBlocker extends PacketBlocker {

    GUIPacketBlocker(PacketBlocker previousBlocker) {
        super(previousBlocker);
    }

    GUIPacketBlocker(UnverifiedUser u) {
        super(u);
    }

    @Override
    public void onPacketReceive(PacketPlayReceiveEvent event) {
        if (!user.hasCompletedCaptcha()) {
            this.onReceiveCaptchaVerification(event);
            return;
        }
        //has completed the captcha and is currently undergoing the pin verification
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                this.virtualFallPhase.trySpoofPackets();
                break;
            case CLOSE_WINDOW:
                event.getPostTasks().add(user::openPasswordBuilderGUI);
                break;
            case CLICK_WINDOW://order of these is important \/
                this.user.getVerificationGUI().select(new WrapperPlayClientClickWindow(event).getSlot());
                this.user.getVerificationGUI().getVirtualGUI().spoofAllItems();
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