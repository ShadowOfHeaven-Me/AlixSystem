package alix.common.antibot.captcha.secrets.cloudflare;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

public final class CloudflareCaptcha {

    public static void makeRequest() {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL("https://challenges.cloudflare.com/turnstile/v0/siteverify").openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}