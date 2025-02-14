package alix.common.login.premium;

import alix.common.data.premium.PremiumData;
import alix.common.utils.collections.list.LoopList;
import alix.common.utils.i18n.HttpsHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import ua.nanit.limbo.util.UUIDUtil;

import java.util.UUID;
import java.util.function.Function;

final class PremiumCheckImpl {

    //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/premium/AuthenticPremiumProvider.java

    static final PremiumCheckImpl INSTANCE = new PremiumCheckImpl();
    private final LoopList<NameCheck> checks;

    private PremiumCheckImpl() {
        this.checks = LoopList.newConcurrent(NameCheck.values());
    }

    @NotNull
    PremiumData fetchPremiumData(String name) {
        PremiumData data;
        int looped = 0;
        while ((data = this.checks.current().isPremium(name)).getStatus().isUnknown()) {
            this.checks.next();
            if (++looped == this.checks.size())
                break;//we couldn't figure it out - return UNKNOWN
        }
        return data;
    }

    private static UUID fromString(String str) {
        return UUIDUtil.fromString(str);
    }

    private enum NameCheck {

        ASHCON(PremiumCheckImpl::getPremiumData_Ashcon),
        PLAYER_DB(PremiumCheckImpl::getPremiumData_PlayerDB),
        MOJANG(PremiumCheckImpl::getPremiumData_Mojang);

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