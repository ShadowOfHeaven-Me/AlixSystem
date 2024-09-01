package alix.common.antibot.captcha.secrets.files;

import java.io.File;

public final class SecretsFileManager {

    private static final SecretsFile file = new SecretsFile();
    public static final String CLOUDFLARE_SITE_KEY = file.getConfig().getString("cloudflare-site-key");
    public static final String CLOUDFLARE_SECRET_KEY = file.getConfig().getString("cloudflare-secret-key");

    public static File getSecretsFile() {
        return file.getFile();
    }
}