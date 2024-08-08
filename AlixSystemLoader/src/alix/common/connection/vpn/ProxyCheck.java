package alix.common.connection.vpn;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public interface ProxyCheck {

    CheckResult isProxy(String address);

    static JsonElement getResponse(String urlLink) {
        HttpsURLConnection connection = null;
        try {
            JsonElement out;
            URL url = new URL(urlLink);
            connection = (HttpsURLConnection) url.openConnection();
            if (connection.getResponseCode() == 200) {//HttpsURLConnection.HTTP_OK
                try (InputStream inputStream = connection.getInputStream()) {
                    out = new JsonParser().parse(new InputStreamReader(inputStream));
                }
            } else {
                connection.disconnect();//make sure we close the connection
                return null;//probably out of queries or maybe the server is down
            }
            return out;
        } catch (Exception e) {
            return null;//some sort of error occurred, assume the service is unavailable
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}