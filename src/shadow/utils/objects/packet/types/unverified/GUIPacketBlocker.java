package shadow.utils.objects.packet.types.unverified;

import alix.common.scheduler.AlixScheduler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.TimeUnit;

public class GUIPacketBlocker extends PacketBlocker {

    protected GUIPacketBlocker(UnverifiedUser u) {
        super(u);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (user.hasCompletedCaptcha()) {//has completed the captcha and is currently undergoing the pin verification
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayInCloseWindow":
                    //super.channelReadNotOverridden(ctx, msg);
                    AlixScheduler.runLaterSync(user::openPasswordBuilderGUI, 100, TimeUnit.MILLISECONDS);
                    return;
                case "PacketPlayInWindowClick":
                case "PacketPlayInKeepAlive":
                    super.channelReadNotOverridden(ctx, msg); //sends packets only if they are pin-related or necessary in other ways
                    return;
            }
            return;
        }
        super.captchaVerification(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //if (spoofedWindowItems(msg)) return;
        if (user.hasCompletedCaptcha()) {
            switch (msg.getClass().getSimpleName()) {
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

        switch (msg.getClass().getSimpleName()) {
            case "PacketPlayOutRelEntityMove":
            case "PacketPlayOutNamedEntitySpawn":
            case "PacketPlayOutSpawnEntityLiving":
            case "PacketPlayOutSpawnEntity":
            case "PacketPlayOutEntityMetadata":
            case "PacketPlayOutEntityEquipment":
            case "PacketPlayOutEntityHeadRotation":
            case "PacketPlayOutEntityStatus":
            case "PacketPlayOutEntityVelocity":
            case "PacketPlayOutEntityDestroy":
            case "PacketPlayOutEntityLook":
            case "PacketPlayOutPlayerInfo":
            case "ClientboundPlayerInfoUpdatePacket":
            case "ClientboundBundlePacket":
                return;
        }
        super.writeNotOverridden(ctx, msg, promise);
    }
}