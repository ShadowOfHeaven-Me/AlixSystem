package shadow.utils.holders.skull;

import org.bukkit.inventory.ItemStack;

public interface SkullSupplier {

    ItemStack createSkull(String url);

    static SkullSupplier createImpl() {
        try {
            Class.forName("org.bukkit.profile.PlayerProfile");
            return new APISkullSupplier();//necessary because of an annoying message from bukkit
        } catch (ReflectiveOperationException e) {
            return new ReflectionSkullSupplier();
        }
    }
}