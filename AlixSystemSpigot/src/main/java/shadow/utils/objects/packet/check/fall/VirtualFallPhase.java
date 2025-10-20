package shadow.utils.objects.packet.check.fall;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import io.netty.buffer.ByteBuf;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutPositionPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;

public final class VirtualFallPhase {

    //private static final double MIN_Y = AlixWorld.TELEPORT_LOCATION.getY() - 10;
    private static final int NOT_FALLING_TELEPORT_ID = AlixUtils.getRandom(13456789, 34567898) + 96;// ;]
    private static final int TIMEOUT = BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND * 15;//timeout the player after 15 seconds
    public static final ByteBuf FALL_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_FALL_LOCATION, 10);
    public static final ByteBuf NOT_FALLING_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_LOCATION, NOT_FALLING_TELEPORT_ID);
    //private static final ByteBuf BARRIER = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I.add(0, -1, 0), SpigotConversionUtil.fromBukkitBlockData(Material.DIRT.createBlockData()).getGlobalId()));
    //private static final ByteBuf NO_COBWEB = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I, SpigotConversionUtil.fromBukkitBlockData(Material.AIR.createBlockData()).getGlobalId()));

    //private static final ByteBuf BARRIER1 = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I, SpigotConversionUtil.fromBukkitBlockData(Material.BARRIER.createBlockData()).getGlobalId()));
    //private static final ByteBuf BARRIER2 = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I.add(0, 1, 0), SpigotConversionUtil.fromBukkitBlockData(Material.BARRIER.createBlockData()).getGlobalId()));

    //public static final ByteBuf BLOCK_SET_AIR = new WrapperPlayServerBlockChange(new Vector3i(0, 2, 0), 0);
    //public static final ByteBuf FALL_STOP_SOUND = OutSoundPacketConstructor
    //public static final ByteBuf FALL_CHECK_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(0, 100, 0));
    //private static final ByteBuf END_PORTAL = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I.add(0, 1, 0), SpigotConversionUtil.fromBukkitBlockData(Material.END_GATEWAY.createBlockData()).getGlobalId()));
    //private static final ByteBuf END_PORTAL2 = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I.add(0, 2, 0), SpigotConversionUtil.fromBukkitBlockData(Material.END_GATEWAY.createBlockData()).getGlobalId()));
    //private static final ByteBuf END_PORTAL3 = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I.add(0, 3, 0), SpigotConversionUtil.fromBukkitBlockData(Material.END_GATEWAY.createBlockData()).getGlobalId()));


    private final UnverifiedUser user;
    //private long sendTime;//, time;
    //private long lastCorrectingTeleport;
    private int tillTimeout;
    private byte waitPackets;
    private boolean packetsSent, tpConfirmReceived, firstPlayPacketReceived;//, noCobwebSent, chunkSent;

    public VirtualFallPhase(UnverifiedUser user) {
        this.user = user;
        this.tillTimeout = TIMEOUT;

        //this.user.writeAndFlushDynamicSilently(new WrapperPlayServerPlayerAbilities());
        //this.time = System.currentTimeMillis();
        //Main.logError("MMMMMMMMMM " + user.reetrooperUser().getDecoderState());
        //this.user.writeAndFlushConstSilently(FALL_CHECK_TELEPORT);
    }

    private static final ByteBuf PLAYER_ABILITIES_FALL_PACKET = NettyUtils.constBuffer(new WrapperPlayServerPlayerAbilities(true, false, false, false, 0.0f, 0.0f));//default: flySpeed = 0.05f, fovModifier = 0.1f

    public void playPhase() {
        if (firstPlayPacketReceived) return;
        this.firstPlayPacketReceived = true;

        //Main.debug(this.user.getChannel().pipeline().names() + " ");

        //this.user.writeDynamicSilently(new WrapperPlayServerEntityAnimation(user.getPlayer().getEntityId(), WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM));
        this.user.writeConstSilently(PLAYER_ABILITIES_FALL_PACKET);
        this.user.writeAndFlushConstSilently(FALL_TELEPORT);
        //this.user.keepAliveSent = System.currentTimeMillis();
        //user.writeAndFlushDynamicSilently(new WrapperPlayServerKeepAlive(2137));
        //this.t = System.currentTimeMillis();
    }

    public void tpPosCorrect() {
        this.user.writeAndFlushConstSilently(NOT_FALLING_TELEPORT);
        //this.lastCorrectingTeleport = System.currentTimeMillis();
    }

/*    public void noCobwebSpoof() {
        //this.user.writeAndFlushConstSilently(NO_COBWEB);
    }*/

    public void trySpoofPackets(PacketPlayReceiveEvent event) {
        //Main.debug("MOVE RECEIVEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        //this.user.sendDynamicMessageSilently(AlixUtils.getFields(this));
        //Main.logError(AlixUtils.getFields(this));

/*        if (tpConfirmReceived) {
            if (this.noCobwebSent) return;
            *//*if (this.noCobwebSent) {
                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
                if (flying.hasPositionChanged() && flying.getLocation().getY() < MIN_Y) {
                    long now = System.currentTimeMillis();
                    if (now - this.lastCorrectingTeleport < 5000)
                        return;//try to prevent any exploits by limiting how often the correcting teleport can occur
                    this.tpPosCorrect();
                }
                return;
            }*//*

            if (++this.waitPackets == 5) {
                this.noCobwebSent = true;
                this.noCobwebSpoof();
                //this.user.sendDynamicMessageSilently("NO COBWEB");
            }
            return;
        }*/

        if (packetsSent) return;

        if (this.waitPackets != 5) {
            this.waitPackets++;
            return;
        }

        //long time = System.currentTimeMillis() - t;
        //Main.debug("TIME MILLIS TO COMPLETE FALL: " + time);
        this.waitPackets = 0;
        this.packetsSent = true;
        this.user.spoofVerificationPackets();
        //this.user.writeConstSilently(END_PORTAL);
        //this.user.writeConstSilently(END_PORTAL2);
        //this.user.writeConstSilently(END_PORTAL3);
        this.user.writeAndFlushConstSilently(NOT_FALLING_TELEPORT);
    }

    //long t;

    public void setChunkSent() {
        //this.chunkSent = true;
    }

    //returns true if should be kicked out
    public boolean timeoutTick() {
        return !tpConfirmReceived && --tillTimeout == 0;
    }

    public boolean isOngoing() {
        return !packetsSent;
    }

    public void tpConfirm(PacketPlayReceiveEvent event) {
        if (tpConfirmReceived) return;
        WrapperPlayClientTeleportConfirm wrapper = new WrapperPlayClientTeleportConfirm(event);
/*        if (wrapper.getTeleportId() == 10) {
            Main.debug("FALL TP CONFIRM ");
        }*/
        if (wrapper.getTeleportId() == NOT_FALLING_TELEPORT_ID) {
            this.tpConfirmReceived = true;
            //Main.debug("FALL TP CONFIRM--------------------------EWFFFFFF ");
        }
    }
}