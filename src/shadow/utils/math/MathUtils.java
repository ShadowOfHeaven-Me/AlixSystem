package shadow.utils.math;


import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public final class MathUtils {

    private static final CircleFastMath circleMath = CircleFastMath.circleFastMath;

    public static Vector3d getFacedLocation(Location l, double distance) {
        return l.getPosition().add(getDirection(l).normalize().multiply(distance));
    }

    public static Vector3d getDirection(Location l) {
        double rotX = l.getYaw();
        double rotY = l.getPitch();
        double y = -Math.sin(Math.toRadians(rotY));
        double xz = Math.cos(Math.toRadians(rotY));
        double x = -xz * Math.sin(Math.toRadians(rotX));
        double z = xz * Math.cos(Math.toRadians(rotX));
        return new Vector3d(x, y, z);
    }

    public static Vector3d pointOnACircle(Vector3d center, double radius, int degrees) {
        double[] xArray = circleMath.getArrayOfXes();
        double[] zArray = circleMath.getArrayOfZs();

        double x = center.getX();
        double y = center.getY();
        double z = center.getZ();

        double x1 = x + xArray[degrees] * radius;
        double z1 = z + zArray[degrees] * radius;

        return new Vector3d(x1, y, z1);
    }

    public static List<Vector> circlePointsAround(Vector center, double radius, int degreeAddition) {
        List<Vector> list = new ArrayList<>();

        double[] xArray = circleMath.getArrayOfXes();
        double[] zArray = circleMath.getArrayOfZs();

        double x = center.getX();
        double y = center.getY();
        double z = center.getZ();

        for (int degree = 0; degree < 360; degree += degreeAddition) {
            double x1 = x + xArray[degree] * radius;
            double z1 = z + zArray[degree] * radius;
            list.add(new Vector(x1, y, z1));
        }
        return list;
    }
}