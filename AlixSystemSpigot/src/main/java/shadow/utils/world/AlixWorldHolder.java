package shadow.utils.world;

import alix.common.utils.other.throwable.AlixException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public final class AlixWorldHolder {//This class (like the bukkit Location class) allows the World object to be finalized

    private static Reference<World> ref = newRef();

    public static World getMain() {
        World w = ref.get();
        return w != null ? w : (ref = newRef()).get();
    }

    public static boolean isMain(World world) {
        return getMain().getUID().equals(world.getUID());
    }

    //let the world be garbage collected by holding it in a weak reference
    private static Reference<World> newRef() {
        World w = Bukkit.getWorld("world");
        if (w != null) return new WeakReference<>(w);

        for (World world : Bukkit.getWorlds())
            if (world.getEnvironment() == World.Environment.NORMAL && !world.getName().equals(AlixWorld.worldName))
                return new WeakReference<>(world);

        for (World world : Bukkit.getWorlds())
            if (!world.getName().equals(AlixWorld.worldName))
                return new WeakReference<>(world);

        //w = AlixWorld.CAPTCHA_WORLD;
        if (w == null) throw new AlixException("Couldn't find any worlds!");

        return new WeakReference<>(w);
    }

    private AlixWorldHolder() {
    }
}