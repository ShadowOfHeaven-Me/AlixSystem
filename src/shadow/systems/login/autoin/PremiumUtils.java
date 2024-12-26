package shadow.systems.login.autoin;

import alix.common.data.premium.PremiumData;
import alix.common.utils.i18n.HttpsHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ua.nanit.limbo.util.UUIDUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Function;

public final class PremiumUtils {

    //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/premium/AuthenticPremiumProvider.java

    public static PremiumData getPremiumData(String name) {
        for (NameCheck check : NameCheck.values()) {
            PremiumData premium = check.isPremium(name);
            if (premium.getStatus().isUnknown()) continue;

            return premium;
        }
        return PremiumData.UNKNOWN;//we don't know
    }

    private static UUID fromString(String str) {
        return UUIDUtil.fromString(str);
    }

    private enum NameCheck {

        ASHCON(PremiumUtils::getPremiumData_Ashcon),
        PLAYER_DB(PremiumUtils::getPremiumData_PlayerDB),
        MOJANG(PremiumUtils::getPremiumData_Mojang);

        private final Function<String, PremiumData> nameCheck;

        NameCheck(Function<String, PremiumData> nameCheck) {
            this.nameCheck = nameCheck;
        }

        @NotNull
        private PremiumData isPremium(String name) {
            return this.nameCheck.apply(name);
        }
    }

    @NotNull
    private static PremiumData getPremiumData_Mojang(String name) {
        JsonElement element = HttpsHandler.getResponse("https://api.mojang.com/users/profiles/minecraft/" + name);
        if (element == null) return PremiumData.UNKNOWN;

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("id")) return PremiumData.NON_PREMIUM;//we can assume that it's nonpremium

        String id = obj.get("id").getAsString();//undashed

        return PremiumData.createNew(fromString(id));
    }

    @NotNull
    private static PremiumData getPremiumData_PlayerDB(String name) {
        JsonElement element = HttpsHandler.getResponse("https://playerdb.co/api/player/minecraft/" + name);
        if (element == null) return PremiumData.UNKNOWN;

        //JsonElement premium = element.getAsJsonObject().get("success");//true for premium, false for nonpremium
        //return premium != null ? premium.getAsBoolean() : null;

        JsonObject obj = element.getAsJsonObject();

        if (!obj.has("success")) return PremiumData.UNKNOWN;//something went wrong
        if (!obj.get("success").getAsBoolean()) return PremiumData.NON_PREMIUM;//we can assume that it's nonpremium

        String id = obj.get("data").getAsJsonObject().get("player").getAsJsonObject().get("id").getAsString();//dashed

        return PremiumData.createNew(fromString(id));
    }

    @NotNull
    private static PremiumData getPremiumData_Ashcon(String name) {
        JsonElement element = HttpsHandler.getResponse("https://api.ashcon.app/mojang/v2/user/" + name);
        if (element == null) return PremiumData.UNKNOWN;

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("uuid")) return PremiumData.NON_PREMIUM;//we can assume that it's nonpremium

        String id = obj.get("uuid").getAsString();//dashed

        return PremiumData.createNew(fromString(id));
    }
}