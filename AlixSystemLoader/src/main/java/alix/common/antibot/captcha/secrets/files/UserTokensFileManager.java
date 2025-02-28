package alix.common.antibot.captcha.secrets.files;

import alix.common.utils.other.keys.secret.MapSecretKey;

import java.util.function.Supplier;

public final class UserTokensFileManager {

    private static final UserTokensFile file = new UserTokensFile();

    public static void setToken(MapSecretKey key, String token) {
        file.getMap().put(key, token);
    }

    public static String getToken(MapSecretKey key) {
        return file.getMap().get(key);
    }

    public static String getTokenOrSupply(MapSecretKey key, Supplier<String> tokenSupplier) {
        return file.getMap().computeIfAbsent(key, k -> tokenSupplier.get());
    }

    public static void save() {
        file.save();
    }

    static {
        file.loadExceptionless();
    }

    public static void init() {
    }
}