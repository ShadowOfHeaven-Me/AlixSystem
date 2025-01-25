package shadow.systems.login.captcha.consumer;

import alix.common.messages.Messages;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import shadow.systems.commands.CommandManager;
import shadow.systems.login.captcha.subtypes.ReCaptcha;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;

import java.awt.*;

import static shadow.utils.math.MathUtils.gcd;
import static shadow.utils.math.MathUtils.round;

public final class ReCaptchaConsumer extends AbstractCaptchaConsumer<ReCaptcha> {

    private static final Vector3d PLAYER_HEAD_POS = AlixWorld.TELEPORT_VEC3D.add(0, 1.62, 0);
    private float
            lastYaw = AlixWorld.TELEPORT_LOCATION.getYaw(),
            lastPitch = AlixWorld.TELEPORT_LOCATION.getPitch(),
            lastDeltaYaw, lastDeltaPitch;
    //private Location lastLoc;

    ReCaptchaConsumer(UnverifiedUser user) {
        super(user);
    }

    int rotations;

    @Override
    public void onMove(PacketPlayReceiveEvent event) {
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

/*        switch (event.getPacketType()) {
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
                wrapper = new WrapperPlayClientPlayerFlying(event);
                break;
            case PLAYER_FLYING:
                wrapper = new WrapperPlayClientPlayerFlying(event);
                if (!wrapper.hasRotationChanged()) return;
                break;
            default:
                return;
        }*/
        //Thread.currentThread().setContextClassLoader();

        if (wrapper.hasRotationChanged()) {
            Location loc = wrapper.getLocation();

            float deltaYaw = round(Math.abs(loc.getYaw() - this.lastYaw), 4);
            float deltaPitch = round(Math.abs(loc.getPitch() - this.lastPitch), 4);
            //this.user.sendDynamicMessageSilently("GCD both: " + gcd(deltaYaw, deltaPitch));

            float gcdPitch = gcd(deltaPitch, this.lastDeltaPitch);
            float gcdYaw = gcd(deltaYaw, this.lastDeltaYaw);

            float modulo = Math.max(gcdPitch, gcdYaw) % Math.min(gcdPitch, gcdYaw);

            //String s = "GCD PITCH: " + gcdPitch + " GCD YAW: " + gcdYaw + " GCD both: " + gcd(deltaYaw, deltaPitch) + " MODULO: " + modulo;// + " DELTA YAW: " + deltaYaw + " DELTA PITCH: " + deltaPitch;
            if (gcdPitch > 1.0E-4 && gcdYaw > 1.0E-4) this.user.debug("deltaYaw: " + deltaYaw + " deltaPitch: " + deltaPitch + " gcdPitch: " + gcdPitch + " gcdYaw: " + gcdYaw + " modulo: " + modulo + " ROTATIONS: " + this.rotations);

            this.lastDeltaPitch = deltaPitch;
            this.lastDeltaYaw = deltaYaw;
        }

        if (wrapper.hasRotationChanged()) {
            this.lastPitch = wrapper.getLocation().getPitch();
            this.lastYaw = wrapper.getLocation().getYaw();
            this.rotations++;
            //this.user.debug("ROTATIONS: " + this.rotations);
        }

        /*if (wrapper.hasPositionChanged()) {
            this.user.debug("POS CHANGEEEEED");
            //this.lastPos = wrapper.getLocation().getPosition();
        }*/
    }

    private static final ByteBuf captchaFailedKickPacket = OutDisconnectPacketConstructor.constructConstAtPlayPhase(Messages.get("captcha-fail-kick"));
    private static final ByteBuf captchaFailedMessagePacket = OutMessagePacketConstructor.constructConst(Messages.getWithPrefix("captcha-fail-chat"));

    private static final double dist = SpigotConversionUtil.fromBukkitLocation(ReCaptcha.HEAD_LOC).getPosition().distance(PLAYER_HEAD_POS);

    @Override
    public void onClick() {
        //if (this.lastPos == null) return;

       /* org.bukkit.Location playerLoc = new org.bukkit.Location(AlixWorld.CAPTCHA_WORLD, this.lastPos.getX(), this.lastPos.getY(), this.lastPos.getZ(), this.lastYaw, this.lastPitch);
        double dist = playerLoc.clone().add(0, 1.62, 0).distance(ReCaptcha.HEAD_LOC);

        org.bukkit.Location faced = AlixUtils.getFacedLocation(playerLoc, dist);
        double clickDistFromButton = faced.distance(ReCaptcha.HEAD_LOC);*/

        org.bukkit.Location faced = AlixUtils.getFacedLocation(new org.bukkit.Location(AlixWorld.CAPTCHA_WORLD, PLAYER_HEAD_POS.getX(), PLAYER_HEAD_POS.getY(), PLAYER_HEAD_POS.getZ(), this.lastYaw, this.lastPitch), dist);

        //this.user.writeAndFlushDynamicSilently(particle(Color.GREEN, this.lastPos));
        //this.user.writeAndFlushDynamicSilently(particle(Color.RED, faced));

        double clickDist = faced.distance(ReCaptcha.HEAD_LOC);

        //String s = "CLICK DISTANCE: " + clickDist + " HEADS DIST: " + dist + " CLICK VECTOR: " + clickVector;
        if (clickDist < 0.21) {
            //this.user.debug("VALID CLICK");
            if (!this.captchaSuccess()) {
                if (++this.user.captchaAttempts == AlixUtils.maxCaptchaAttempts) MethodProvider.kickAsync(this.user, captchaFailedKickPacket);
                else {
                    this.user.writeConstSilently(captchaFailedMessagePacket);
                    this.user.getPacketBlocker().getFallPhase().tpPosCorrect();
                    this.rotations = 0;
                }
                return;
            }
            this.user.writeConstSilently(CommandManager.captchaCompleteMessagePacket);
            this.user.completeCaptcha();
        }
    }

    private boolean captchaSuccess() {
        if (this.rotations < 10) return false;
        return true;
    }

    public static WrapperPlayServerParticle particle(Color color, org.bukkit.Location pos) {
        return particle(color, SpigotConversionUtil.fromBukkitLocation(pos));
    }

    public static WrapperPlayServerParticle particle(Color color, Location pos) {
        return particle(color, pos.getPosition());
    }

    public static WrapperPlayServerParticle particle(Color color, Vector3d pos) {
        Particle particle = new Particle(ParticleTypes.DUST, new ParticleDustData(1.5f, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f));
        return new WrapperPlayServerParticle(particle, false, pos, Vector3f.zero(), 0, 1);
    }

/*    @Override
    public Location getLocation() {
        return null;
    }*/
}