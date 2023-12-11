package alix.common.utils.i18n;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class HttpsHandler {

    public static JsonElement readURL(String uri) throws IOException {
        JsonElement output = null;
        URL url = new URL(uri);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        if (connection.getResponseCode() == 200) {//HttpsURLConnection.HTTP_OK
            try (InputStream inputStream = connection.getInputStream()) {
                output = new JsonParser().parse(new InputStreamReader(inputStream));
            }
        }
        connection.disconnect();
        return output;
    }

    public static String getHtmlResponse(String uri) {
        try {
            URL url = new URL(uri);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try (InputStream is = connection.getInputStream()) {
                StringBuilder response = new StringBuilder();
                try (Scanner scanner = new Scanner(is)) {
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }
                }
                return response.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUrlResponse(String url, String jsonInputString) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);


            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
            }
            conn.disconnect();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String queryURL(String query) {
        try {
            for (int i = 0; i < 3; i++) {
                try {
                    HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.mineskin.org/generate/url/").openConnection();

                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-length", String.valueOf(query.length()));
                    con.setRequestProperty("Accept", "application/json");
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    con.setRequestProperty("User-Agent", "SkinsRestorer");
                    con.setConnectTimeout(90000);
                    con.setReadTimeout(90000);
                    con.setDoOutput(true);
                    con.setDoInput(true);

                    DataOutputStream output = new DataOutputStream(con.getOutputStream());
                    output.writeBytes(query);
                    output.close();
                    StringBuilder outStr = new StringBuilder();
                    InputStream is;

                    try {
                        is = con.getInputStream();
                    } catch (Exception e) {
                        is = con.getErrorStream();
                    }

                    DataInputStream input = new DataInputStream(is);
                    for (int c = input.read(); c != -1; c = input.read())
                        outStr.append((char) c);

                    input.close();
                    return outStr.toString();
                } catch (IOException e) {
                    if (i == 2)
                        throw e;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        throw new RuntimeException("IO");
    }

    private HttpsHandler() {
    }
}