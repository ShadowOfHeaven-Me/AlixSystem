package shadow.utils.misc.skull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import shadow.utils.misc.version.AlixMaterials;

import java.lang.reflect.Field;
import java.util.UUID;

class ReflectionSkullSupplier implements SkullSupplier {

    @Override
    public ItemStack createSkull(String url) {
        ItemStack head = AlixMaterials.PLAYER_HEAD.getItemCloned();
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), "none");
        profile.getProperties().put("textures", new Property("textures", url));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        head.setItemMeta(headMeta);
        return head;
    }
}