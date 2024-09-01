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

    private static final int NOT_FALLING_TELEPORT_ID = AlixUtils.getRandom(13456789, 34567898) + 96;// ;]
    private static final int TIMEOUT = BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND * 10;//timeout the player after 10 seconds
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
    private int tillTimeout;
    private byte waitPackets;
    private boolean packetsSent, tpConfirmReceived, firstPlayPacketReceived, blocksSent;

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

    public void trySpoofPackets(PacketPlayReceiveEvent event) {
        //Main.logError("PACKET COUNT: " + (waitPackets + 1));

        if (tpConfirmReceived) {
            //this.user.sendDynamicMessageSilently("TRY SPOOF: " +blocksSent + " " + waitPackets);

            if (this.blocksSent) return;

            if (++this.waitPackets == 5) {
                this.blocksSent = true;
                //this.user.writeConstSilently(BARRIER1);
                //this.user.writeAndFlushConstSilently(BARRIER2);
                this.user.writeAndFlushConstSilently(NO_COBWEB);
                //this.user.sendDynamicMessageSilently("HEHEHEHEH");
            }
            return;
        }

        if (packetsSent) return;

        //Main.logError("LOC: " + this.user.getPlayer().getLocation());
        //if (++waitPackets == 3) user.writeConstSilently(OutGameStatePacketConstructor.ADVENTURE_GAMEMODE_PACKET);

        if (++this.waitPackets == 20) {
            this.waitPackets = 0;
            //Main.logError("REACHED AFTER " + (System.currentTimeMillis() - time));
            this.packetsSent = true;
            //this.user.writeAndFlushConstSilently(BARRIER);
            //Main.logError("NOT FALLING TP");
            this.user.writeAndFlushConstSilently(NOT_FALLING_TELEPORT);
            //this.user.writeConstSilently(NO_COBWEB);


            //this.user.getPlayer().stopSound(Sound.ENTITY_GENERIC_SMALL_FALL);//as much as it pains me, it can only be done this way for now
            //this.user.getPlayer().stopSound(Sound.ENTITY_PLAYER_SMALL_FALL);
            //this.sendTime = System.currentTimeMillis();
            this.user.spoofVerificationPackets();
        }
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

    //TODO: Find out why in login this isn't ever invoked
    //yeah, turns out because I only invoked it during the captcha part of code
    //Nice.
    public void tpConfirm(PacketPlayReceiveEvent event) {
        if (tpConfirmReceived) return;
        //long time = System.currentTimeMillis();
        WrapperPlayClientTeleportConfirm wrapper = new WrapperPlayClientTeleportConfirm(event);
        if (wrapper.getTeleportId() == NOT_FALLING_TELEPORT_ID) {
            this.tpConfirmReceived = true;
            //this.user.sendDynamicMessageSilently("TP CONFIRM RECEIVED");
            //this.user.writeAndFlushConstSilently(NO_COBWEB);
            //Main.logError("NO COBWEB");
            //int ping = (int) ((time - this.sendTime) >> 1);
            //Main.logError("PING: " + ((time - this.sendTime) >> 1));
            //Main.logError("TP CONFIRM");
        }
    }
}