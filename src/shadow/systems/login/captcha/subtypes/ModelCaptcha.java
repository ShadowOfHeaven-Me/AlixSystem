package shadow.systems.login.captcha.subtypes;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import io.netty.buffer.ByteBuf;
import shadow.Main;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.math.MathUtils;
import shadow.utils.misc.captcha.ImageRenderer;
import shadow.utils.users.types.UnverifiedUser;

import java.util.HashMap;
import java.util.Map;

public final class ModelCaptcha extends Captcha {

    //private final Map<Vector, ByteBuf> movement = new HashMap<>(16, 1.0f);
    private final Map<Vector3d, ByteBuf[]> selectablePointsToUpdateBufs = new HashMap<>();
    private final ByteBuf[] buffers;
    private volatile boolean injected;

    public ModelCaptcha() {
        //BufferedImage image = CaptchaImageGenerator.generateCaptchaImageX256(captcha, maxRotation, false, false);
        this.buffers = ImageRenderer.model3dBuffers(this.selectablePointsToUpdateBufs);
    }

    public void onClick(UnverifiedUser user, Location loc) {
        Vector3d faced = MathUtils.getFacedLocation(loc, 10);

        Main.logError("FACED: " + faced + " LOC: " + loc);

        this.selectablePointsToUpdateBufs.keySet().forEach(v -> Main.logError("SELECTABLE: " + v));

        ByteBuf[] closest = null;
        double cDist = 100;

        for (Map.Entry<Vector3d, ByteBuf[]> e : selectablePointsToUpdateBufs.entrySet()) {
            double distance = Math.hypot(e.getKey().getX() - faced.getX(), e.getKey().getZ() - faced.getZ());
            if (distance < cDist) {
                cDist = distance;
                closest = e.getValue();
            }
        }

        Main.logError("CLOSEST: " + cDist + " " + closest.length);

        //user.writeAndFlushSilently(closest[0].copy());
        for (ByteBuf buf : closest) user.writeAndFlushSilently(buf.copy());
        //user.flushSilently();
        //user.flushSilently();
    }

    @Override
    public void sendPackets(UnverifiedUser user) {
        this.injected = true;
        for (ByteBuf buf : this.buffers) user.writeSilently(buf);
    }

    @Override
    public void release() {
        if (injected) return;
        this.injected = true;
        for (ByteBuf buf : this.buffers) if (buf.refCnt() != 0) buf.release();
    }
}