package shadow.utils.world;

import alix.common.utils.other.throwable.AlixException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class ConstLocation extends Location {

    public ConstLocation(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    @Override
    public Location setDirection(Vector vector) {
        if (!super.getDirection().equals(vector)) throw new AlixException("Cannot modify a Constant Location!");
        return this;
    }

    @Override
    public void setWorld(World world) {
        if (!super.getWorld().equals(world)) throw new AlixException("Cannot modify a Constant Location!");
    }

    @Override
    public void setX(double x) {
        if (Double.compare(super.getX(), x) != 0) throw new AlixException("Cannot modify a Constant Location!");
    }

    @Override
    public void setY(double y) {
        if (Double.compare(super.getY(), y) != 0) throw new AlixException("Cannot modify a Constant Location!");
    }

    @Override
    public void setZ(double z) {
        if (Double.compare(super.getZ(), z) != 0) throw new AlixException("Cannot modify a Constant Location!");
    }

    @Override
    public void setYaw(float yaw) {
        if (Float.compare(super.getYaw(), yaw) != 0) throw new AlixException("Cannot modify a Constant Location!");
    }

    @Override
    public void setPitch(float pitch) {
        if (Float.compare(super.getPitch(), pitch) != 0) throw new AlixException("Cannot modify a Constant Location!");
    }
}