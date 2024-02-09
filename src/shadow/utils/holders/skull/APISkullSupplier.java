package shadow.utils.holders.skull;

import alix.common.utils.other.throwable.AlixError;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import shadow.utils.holders.ReflectionUtils;


import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

//Thanks mfnalex ^^
//Source code: https://blog.jeff-media.com/creating-custom-heads-in-spigot-1-18-1/
class APISkullSupplier implements SkullSupplier {

    private final Method createProfile = ReflectionUtils.getMethod(Bukkit.class, "createPlayerProfile", UUID.class);
    private final Method setPlayerProfile = ReflectionUtils.getMethod(SkullMeta.class, "setOwnerProfile", PlayerProfile.class);

    @Override
    public ItemStack createSkull(String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        try {
            PlayerProfile profile = (PlayerProfile) this.createProfile.invoke(null, UUID.randomUUID());
            profile.getTextures().setSkin(getUrlFromBase64(url));
            this.setPlayerProfile.invoke(headMeta, profile);
            head.setItemMeta(headMeta);
            return head;
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }

    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        // We simply remove the "beginning" and "ending" part of the JSON, so we're left with only the URL. You could use a proper
        // JSON parser for this, but that's not worth it. The String will always start exactly with this stuff anyway
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }
}