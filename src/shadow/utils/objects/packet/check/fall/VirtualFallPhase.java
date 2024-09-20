package shadow.utils.objects.packet.check.fall;

import alix.common.utils.other.annotation.ScheduledForFix;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import org.bukkit.Material;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutPositionPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;

public final class VirtualFallPhase {

    //private static final double MIN_Y = AlixWorld.TELEPORT_LOCATION.getY() - 10;
    private static final int NOT_FALLING_TELEPORT_ID = AlixUtils.getRandom(13456789, 34567898) + 96;// ;]
    private static final int TIMEOUT = BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND * 7;//timeout the player after 7 seconds
    public static final ByteBuf FALL_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_FALL_LOCATION, 10);
    public static final ByteBuf NOT_FALLING_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_LOCATION, NOT_FALLING_TELEPORT_ID);
    //private static final ByteBuf BARRIER = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I.add(0, -1, 0), SpigotConversionUtil.fromBukkitBlockData(Material.DIRT.createBlockData()).getGlobalId()));
    private static final ByteBuf NO_COBWEB = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I, SpigotConversionUtil.fromBukkitBlockData(Material.AIR.createBlockData()).getGlobalId()));

    //private static final ByteBuf BARRIER1 = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I, SpigotConversionUtil.fromBukkitBlockData(Material.BARRIER.createBlockData()).getGlobalId()));
    //private static final ByteBuf BARRIER2 = NettyUtils.constBuffer(new WrapperPlayServerBlockChange(AlixWorld.TELEPORT_VEC3I.add(0, 1, 0), SpigotConversionUtil.fromBukkitBlockData(Material.BARRIER.createBlockData()).getGlobalId()));

    //public static final ByteBuf BLOCK_SET_AIR = new WrapperPlayServerBlockChange(new Vector3i(0, 2, 0), 0);
    //public static final ByteBuf FALL_STOP_SOUND = OutSoundPacketConstructor
    //public static final ByteBuf FALL_CHECK_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(0, 100, 0));
    private final UnverifiedUser user;
    //private long sendTime;//, time;
    //private long lastCorrectingTeleport;
    private int tillTimeout;
    private byte waitPackets;
    private boolean packetsSent, tpConfirmReceived, firstPlayPacketReceived, noCobwebSent, chunkSent;

    public VirtualFallPhase(UnverifiedUser user) {
        this.user = user;
        this.tillTimeout = TIMEOUT;

        //this.user.writeAndFlushDynamicSilently(new WrapperPlayServerPlayerAbilities());
        //this.time = System.currentTimeMillis();
        //Main.logError("MMMMMMMMMM " + user.reetrooperUser().getDecoderState());
        //this.user.writeAndFlushConstSilently(FALL_CHECK_TELEPORT);
    }

    public void playPhase() {
        if (firstPlayPacketReceived) return;

        this.user.writeAndFlushConstSilently(FALL_TELEPORT);
        this.firstPlayPacketReceived = true;
    }

    public void tpPosCorrect() {
        this.user.writeAndFlushConstSilently(NOT_FALLING_TELEPORT);
        //this.lastCorrectingTeleport = System.currentTimeMillis();
    }

    public void noCobwebSpoof() {
        this.user.writeAndFlushConstSilently(NO_COBWEB);
    }

    public void trySpoofPackets(PacketPlayReceiveEvent event) {
        //this.user.sendDynamicMessageSilently(AlixUtils.getFields(this));
        //Main.logError(AlixUtils.getFields(this));
        if (tpConfirmReceived) {
            if (this.noCobwebSent) return;
            /*if (this.noCobwebSent) {
                WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
                if (flying.hasPositionChanged() && flying.getLocation().getY() < MIN_Y) {
                    long now = System.currentTimeMillis();
                    if (now - this.lastCorrectingTeleport < 5000)
                        return;//try to prevent any exploits by limiting how often the correcting teleport can occur
                    this.tpPosCorrect();
                }
                return;
            }*/

            if (++this.waitPackets == 5) {
                this.noCobwebSent = true;
                this.noCobwebSpoof();
                //this.user.sendDynamicMessageSilently("NO COBWEB");
            }
            return;
        }

        if (packetsSent) return;

        if (this.waitPackets != 20) {
            this.waitPackets++;
            return;
        }

        if (!chunkSent) return;

        this.waitPackets = 0;
        this.packetsSent = true;
        this.user.writeAndFlushConstSilently(NOT_FALLING_TELEPORT);
        //this.user.writeConstSilently(NO_COBWEB);


        //this.user.getPlayer().stopSound(Sound.ENTITY_GENERIC_SMALL_FALL);//as much as it pains me, it can only be done this way for now
        //this.user.getPlayer().stopSound(Sound.ENTITY_PLAYER_SMALL_FALL);
        this.user.spoofVerificationPackets();
    }

    public void setChunkSent() {
        this.chunkSent = true;
    }

    //returns true if should be kicked out

    //Currently uses a pretty unreliable way of determining the timeout
    @ScheduledForFix
    public boolean timeoutTick() {
        return !tpConfirmReceived && --tillTimeout == 0;
    }

    public boolean isOngoing() {
        return !packetsSent;
    }

    public void tpConfirm(PacketPlayReceiveEvent event) {
        if (tpConfirmReceived) return;
        WrapperPlayClientTeleportConfirm wrapper = new WrapperPlayClientTeleportConfirm(event);
        if (wrapper.getTeleportId() == NOT_FALLING_TELEPORT_ID) {
            this.tpConfirmReceived = true;
        }
    }
}