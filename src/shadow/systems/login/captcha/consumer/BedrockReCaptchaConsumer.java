package shadow.systems.login.captcha.consumer;

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
import shadow.Main;
import shadow.systems.login.captcha.subtypes.ConstReCaptcha;
import shadow.systems.login.captcha.subtypes.ReCaptcha;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.world.AlixWorld;

import java.awt.*;

public final class BedrockReCaptchaConsumer extends AbstractCaptchaConsumer<ConstReCaptcha> {

    //private static final Vector3d PLAYER_HEAD_POS = SpigotConversionUtil.fromBukkitLocation(AlixWorld.TELEPORT_LOCATION).getPosition().add(0, 1.62, 0);
    //private static final double dist;
    private static final Vector3d PLAYER_POS = SpigotConversionUtil.fromBukkitLocation(AlixWorld.TELEPORT_LOCATION).getPosition();
    private Vector3d lastPos;
    private float
            lastYaw,
            lastPitch;

    /*static {
        dist = SpigotConversionUtil.fromBukkitLocation(ReCaptcha.HEAD_LOC).getPosition().distance(PLAYER_HEAD_POS);
    }*/

    BedrockReCaptchaConsumer(UnverifiedUser user) {
        super(user);
        this.lastPos = PLAYER_POS;
        this.lastYaw = AlixWorld.TELEPORT_LOCATION.getYaw();
        this.lastPitch = AlixWorld.TELEPORT_LOCATION.getPitch();
    }

    @Override
    public void onMove(PacketPlayReceiveEvent event) {
        WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

        if (wrapper.hasRotationChanged()) {
            this.lastPitch = wrapper.getLocation().getPitch();
            this.lastYaw = wrapper.getLocation().getYaw();
        }

        if (wrapper.hasPositionChanged()) {
            this.lastPos = wrapper.getLocation().getPosition();
        }
    }

    @Override
    public void onClick(Vector3f clickVector) {
        //if (this.lastPos == null) return;

        double dist = SpigotConversionUtil.fromBukkitLocation(ReCaptcha.HEAD_LOC).getPosition().distance(this.lastPos.add(0, 1.62, 0));

        org.bukkit.Location faced = AlixUtils.getFacedLocation(new org.bukkit.Location(AlixWorld.CAPTCHA_WORLD, this.lastPos.getX(), this.lastPos.getY() + 1.62, this.lastPos.getZ(), this.lastYaw, this.lastPitch), dist);

        this.user.writeAndFlushDynamicSilently(particle(Color.GREEN, this.lastPos));
        this.user.writeAndFlushDynamicSilently(particle(Color.RED, faced));

        double clickDist = faced.distance(ReCaptcha.HEAD_LOC);

        String s = "CLICK DISTANCE: " + clickDist + " HEADS DIST: " + dist;
        this.user.sendDynamicMessageSilently(s);
        Main.logError(s);
    }

    public static float gcd(float a, float b) {
        return b == 0 ? a : gcd(b, a % b);
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
}