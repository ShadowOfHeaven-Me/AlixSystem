package shadow.utils.world.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ModifiableLocation extends Location {

    public ModifiableLocation(Location loc) {
        super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public ModifiableLocation(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public ModifiableLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }
    
    public ConstLocation toConst() {
        return new ConstLocation(this);
    }

    @NotNull
    @Override
    public ModifiableLocation setDirection(@NotNull Vector vector) {
        return new ModifiableLocation(super.setDirection(vector));
    }

    @NotNull
    @Override
    public ModifiableLocation add(@NotNull Vector vec) {
        return new ModifiableLocation(super.add(vec));
    }

    @NotNull
    @Override
    public ModifiableLocation add(@NotNull Location vec) {
        return new ModifiableLocation(super.add(vec));
    }

    @NotNull
    @Override
    public ModifiableLocation add(double x, double y, double z) {
        return new ModifiableLocation(super.add(x, y, z));
    }

    @NotNull
    @Override
    public ModifiableLocation subtract(@NotNull Location vec) {
        return new ModifiableLocation(super.subtract(vec));
    }

    @NotNull
    @Override
    public ModifiableLocation subtract(@NotNull Vector vec) {
        return new ModifiableLocation(super.subtract(vec));
    }

    @NotNull
    @Override
    public ModifiableLocation multiply(double m) {
        return new ModifiableLocation(super.multiply(m));
    }

    @NotNull
    @Override
    public ModifiableLocation zero() {
        return new ModifiableLocation(super.zero());
    }

    @NotNull
    @Override
    public ModifiableLocation subtract(double x, double y, double z) {
        return new ModifiableLocation(super.subtract(x, y, z));
    }

    @NotNull
    @Override
    public ModifiableLocation clone() {
        return new ModifiableLocation(super.clone());
    }
}