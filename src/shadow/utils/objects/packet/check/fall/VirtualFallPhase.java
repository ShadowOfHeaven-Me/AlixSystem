package shadow.utils.objects.packet.check.fall;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientTeleportConfirm;
import io.netty.buffer.ByteBuf;
import shadow.utils.misc.packet.buffered.BufferedPackets;
import shadow.utils.misc.packet.constructors.OutPositionPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;

import java.util.concurrent.atomic.AtomicBoolean;

public final class VirtualFallPhase {

    private static final int TELEPORT_ID = 96;// ;]
    private static final int TIMEOUT = BufferedPackets.EXPERIENCE_UPDATES_PER_SECOND * 7;//timeout the player after 7 seconds
    public static final ByteBuf CAPTCHA_WORLD_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_LOCATION, TELEPORT_ID);
    //public static final ByteBuf BLOCK_SET_AIR = new WrapperPlayServerBlockChange(new Vector3i(0, 2, 0), 0);
    //public static final ByteBuf FALL_STOP_SOUND = OutSoundPacketConstructor
    //public static final ByteBuf FALL_CHECK_TELEPORT = OutPositionPacketConstructor.constructConst(AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(0, 100, 0));
    private final UnverifiedUser user;
    //private long sendTime;//, time;
    private int tillTimeout;
    private byte waitPackets;
    private boolean packetsSent;
    private final AtomicBoolean tpConfirmReceived = new AtomicBoolean();

    public VirtualFallPhase(UnverifiedUser user) {
        this.user = user;
        this.tillTimeout = TIMEOUT;
        //this.user.writeAndFlushDynamicSilently(new WrapperPlayServerPlayerAbilities());
        //this.time = System.currentTimeMillis();
        //Main.logError("MMMMMMMMMM " + user.reetrooperUser().getDecoderState());
        //this.user.writeAndFlushConstSilently(FALL_CHECK_TELEPORT);
    }

    public void trySpoofPackets() {
        //Main.logError("PACKET COUNT: " + (waitPackets + 1));
        if (packetsSent) return;

        //if (++waitPackets == 3) user.writeConstSilently(OutGameStatePacketConstructor.ADVENTURE_GAMEMODE_PACKET);

        if (++this.waitPackets == 10) {
            //Main.logError("REACHED AFTER " + (System.currentTimeMillis() - time));
            this.packetsSent = true;
            this.user.writeAndFlushConstSilently(CAPTCHA_WORLD_TELEPORT);
            //this.user.getPlayer().stopSound(Sound.ENTITY_GENERIC_SMALL_FALL);//as much as it pains me, it can only be done this way for now
            //this.user.getPlayer().stopSound(Sound.ENTITY_PLAYER_SMALL_FALL);
            //this.sendTime = System.currentTimeMillis();
            this.user.spoofVerificationPackets();
        }
    }

    //returns true if should be kicked out
    public boolean timeoutTick() {
        return !tpConfirmReceived.get() && --tillTimeout == 0;
    }

    public boolean isOngoing() {
        return !packetsSent;
    }

    //TODO: Find out why in login this isn't ever invoked
    public void tpConfirm(PacketPlayReceiveEvent event) {
        if (tpConfirmReceived.get()) return;
        //long time = System.currentTimeMillis();
        WrapperPlayClientTeleportConfirm wrapper = new WrapperPlayClientTeleportConfirm(event);
        if (wrapper.getTeleportId() == TELEPORT_ID) {
            this.tpConfirmReceived.set(true);
            //int ping = (int) ((time - this.sendTime) >> 1);
            //Main.logError("PING: " + ((time - this.sendTime) >> 1));
            //Main.logError("TP CONFIRM");
        }
    }
}