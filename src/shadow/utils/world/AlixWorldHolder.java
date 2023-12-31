package shadow.utils.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public final class AlixWorldHolder {//This class (like the bukkit Location class) allows the World object to be finalized

    private static Reference<World> ref;

    public static World getMain() {
        World w = ref.get();
        return w != null ? w : (ref = newRef()).get();
    }

    public static boolean isMain(World world) {
        return getMain().getUID().equals(world.getUID());
    }

    private static Reference<World> newRef() {
        return new WeakReference<>(Bukkit.getWorlds().get(0));//let the world be garbage collected by holding it in a weak reference
    }

    private AlixWorldHolder() {}

    static {
        ref = newRef();
    }
}