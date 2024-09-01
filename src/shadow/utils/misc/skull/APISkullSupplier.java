package shadow.utils.misc.skull;

import alix.common.utils.other.throwable.AlixError;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import shadow.utils.misc.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

//Thanks mfnalex ^^
//Source code: https://blog.jeff-media.com/creating-custom-heads-in-spigot-1-18-1/
class APISkullSupplier implements SkullSupplier {

    private static final Method createProfile = ReflectionUtils.getMethod(Bukkit.class, "createPlayerProfile", UUID.class);
    private static final Method setPlayerProfile = ReflectionUtils.getMethod(SkullMeta.class, "setOwnerProfile", PlayerProfile.class);

    @Override
    public ItemStack createSkull(String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        try {
            PlayerProfile profile = (PlayerProfile) createProfile.invoke(null, UUID.randomUUID());
            profile.getTextures().setSkin(getUrlFromBase64(url));
            setPlayerProfile.invoke(headMeta, profile);
            head.setItemMeta(headMeta);
            return head;
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }

    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));

        JsonElement json = JsonParser.parseString(decoded);
        String url = json.getAsJsonObject()
                .getAsJsonObject("textures")
                .getAsJsonObject("SKIN")
                .getAsJsonPrimitive("url")
                .getAsString();
        return new URL(url);
        //Main.logError("JSON: " + JsonParser.parseString(decoded));
        //return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }
}