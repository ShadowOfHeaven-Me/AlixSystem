package shadow.utils.world.location;

import alix.common.utils.other.throwable.AlixException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class ConstLocation extends Location {

    public ConstLocation(Location loc) {
        super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public ConstLocation(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public ModifiableLocation asModifiableCopy() {
        return new ModifiableLocation(getWorld(), getX(), getY(), getZ(), getYaw(), getPitch());
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

    @NotNull
    @Override
    public Location add(@NotNull Vector vec) {
        throw new AlixException("Cannot modify a Constant Location!");
    }

    @NotNull
    @Override
    public Location add(@NotNull Location vec) {
        throw new AlixException("Cannot modify a Constant Location!");
    }

    @NotNull
    @Override
    public Location add(double x, double y, double z) {
        throw new AlixException("Cannot modify a Constant Location!");
    }

    @NotNull
    @Override
    public Location subtract(@NotNull Vector vec) {
        throw new AlixException("Cannot modify a Constant Location!");
    }

    @NotNull
    @Override
    public Location subtract(@NotNull Location vec) {
        throw new AlixException("Cannot modify a Constant Location!");
    }

    @NotNull
    @Override
    public Location subtract(double x, double y, double z) {
        throw new AlixException("Cannot modify a Constant Location!");
    }

    @NotNull
    @Override
    public Location multiply(double m) {
        throw new AlixException("Cannot modify a Constant Location!");
    }

    @NotNull
    @Override
    public Location zero() {
        throw new AlixException("Cannot modify a Constant Location!");
    }
}