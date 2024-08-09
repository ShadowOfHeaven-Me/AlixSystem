package alix.common.utils.file.update;

import alix.common.AlixCommonMain;
import alix.loaders.bukkit.BukkitAlixMain;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public final class UpdateChecker {

    public static void checkForUpdates() {
        getVersion(newestVersion -> {
            String currentVersion = BukkitAlixMain.instance.getDescription().getVersion();
            if (isOutdatedVersion(currentVersion, newestVersion))
                AlixCommonMain.logWarning("\n You're running an outdated version of AlixSystem (Using " + currentVersion + " with the newest being " + newestVersion + ")! An update is available at: https://www.spigotmc.org/resources/alixsystem.109144/\n");
        });
    }

    private static boolean isOutdatedVersion(String current, String newest) {
        if (newest.equals(current)) return false;

        String[] newestVersionSplit = newest.split(" ");
        String[] currentVersionSplit = current.split(" ");

        String newestFirstWord = newestVersionSplit[0];
        String currentFirstWord = currentVersionSplit[0];
        //return !actualNewest.equals(newest);

/*        if(currentFirstWord.equals(newestFirstWord) && newestVersionSplit.length != 1 && currentVersionSplit.length != 1) {//these are dev builds, we'll

        }*/

        if (currentFirstWord.equals(newest)) {
            return true;//the currently used version is followed by some other text, like 2.5.0 DEV-1, so when 2.5.0 is out, the 2.5.0 is the newest
        }

        String[] newestVer = newestFirstWord.split("\\.");
        String[] currentVer = currentFirstWord.split("\\.");

        if (newestVer.length != currentVer.length) return true;
        //throw new RuntimeException("Invalid length: " + newestFirstWord + " " + newestVer.length + " - " + current + " " + currentVer.length);
        for (int i = 0; i < newestVer.length; i++) {
            int newestVersionParsed = Integer.parseInt(newestVer[i]);
            int currentVersionParsed = Integer.parseInt(currentVer[i]);

            if (newestVersionParsed < currentVersionParsed)
                return false;//the used version is greater than the one released - must be a dev version, so ahead of releases
            if (newestVersionParsed > currentVersionParsed)
                return true;//the used version is lesser than the newest one - must be outdated
        }
        return false;
    }

    private static void getVersion(Consumer<String> consumer) {
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=109144").openStream();
             Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) consumer.accept(scanner.next());
        } catch (IOException e) {
            AlixCommonMain.logWarning("Could not get the newest version - Check your internet connection!");
        }
    }

    //public static final int RESOURCE_ID = 109144;//on spigot

    /*private static int getLatestDownloadVersionID() throws IOException {
        JsonElement e = HttpsHandler.readURL("https://api.spiget.org/v2/resources/109144/versions/latest");
        return e.getAsJsonObject().get("id").getAsInt();
    }

    public static boolean checkForUpdates(JavaPlugin plugin) {
        StringBuilder sb = new StringBuilder();

        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=109144").openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) sb.append(scanner.next());
        } catch (IOException e) {
            plugin.getLogger().warning("Could not get the newest version - Check your internet connection!");
        }

        String newestVersion = sb.toString();
        String currentVersion = plugin.getDescription().getVersion();


        plugin.getLogger().info("Zetttt 1");

        //if (isOutdatedVersion(currentVersion, newestVersion)) {
            handleOutdatedVersion(plugin);
            plugin.getLogger().info("Zetttt 2");
        //}

        return false;//not outdated
    }

    private static void handleOutdatedVersion(JavaPlugin plugin) {
        try {
            int latestVer = getLatestDownloadVersionID();
            URL url = new URL("https://www.spigotmc.org/resources/alixsystem.109144/download?version=" + latestVer);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setInstanceFollowRedirects(true);
            connection.addRequestProperty("User-Agent", "Mozilla/4.0");
            connection.setDoOutput(false);


            try (InputStream is = connection.getInputStream()) {
                byte[] targetArray = read(is);

                File file = plugin.getDataFolder().toPath().resolve("cache").resolve("AlixSystem.jar").toFile();

                if (!file.exists()) {
                    file.createNewFile();
                }

                Files.write(file.toPath(), targetArray);


//                String inputFile = "jar:file:/c:/path/to/my.jar!/myfile.txt";
                String inputFile = "jar:file:" + file.toPath().toAbsolutePath().toString() + "!/AlixSystem.jarinjar";

                URL inputURL = new URL(inputFile);
                JarURLConnection conn = (JarURLConnection) inputURL.openConnection();
                InputStream in = conn.getInputStream();

                Files.write(file.toPath().getParent().resolve("AlixSystem.jarinjar"), read(in));

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        is.close();

        return buffer.toByteArray();
    }*/
}