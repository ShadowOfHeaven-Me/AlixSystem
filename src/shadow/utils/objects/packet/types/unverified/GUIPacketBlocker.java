package shadow.utils.objects.packet.types.unverified;

import alix.common.scheduler.AlixScheduler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import shadow.Main;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.TimeUnit;

public class GUIPacketBlocker extends PacketBlocker {

    protected GUIPacketBlocker(UnverifiedUser u, PacketInterceptor handler) {
        super(u, handler);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (user.hasCompletedCaptcha()) {//has completed the captcha and is currently undergoing the pin verification
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayInPosition"://most common packets
                case "PacketPlayInPositionLook":
                case "PacketPlayInLook":
                case "d":
                    this.trySpoofPackets();
                    return;
                case "PacketPlayInCloseWindow":
                    //super.channelReadNotOverridden(ctx, msg);
                    AlixScheduler.runLaterSync(user::openPasswordBuilderGUI, 100, TimeUnit.MILLISECONDS);
                    return;
                case "PacketPlayInWindowClick":
                case "PacketPlayInKeepAlive":
                case "ServerboundKeepAlivePacket"://another possible name of this packet on 1.20.2+
                    super.channelReadNotOverridden(ctx, msg); //sends packets only if they are pin-related or necessary in other ways
                    return;
            }
            return;
        }
        super.onReadCaptchaVerification(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //if (spoofedWindowItems(msg)) return;
        if (user.hasCompletedCaptcha()) {
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayOutChat":
                case "ClientboundSystemChatPacket":
                case "ClientboundDisguisedChatPacket":
                case "ClientboundPlayerChatPacket":
                    this.blockedChatPackets.offerLast(msg);
                    return;
                case "PacketPlayOutRespawn":
                case "ClientboundRespawnPacket":
                    this.waitPackets += WAIT_PACKETS_INCREASE;
                    break;
                //case "PacketPlayOutGameStateChange":
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
            super.writeNotOverridden(ctx, msg, promise);
            //Main.logInfo("RECEIVED: " + msg.getClass().getSimpleName());
            return;
        }
        this.onWriteCaptchaVerification(ctx, msg, promise);
    }
}