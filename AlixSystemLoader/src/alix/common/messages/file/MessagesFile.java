package alix.common.messages.file;

import alix.common.AlixCommonMain;
import alix.common.utils.file.AlixFileManager;
import alix.common.utils.formatter.AlixFormatter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class MessagesFile extends AlixFileManager {

    private final Map<String, String> map;

    public MessagesFile() {
        super(findFile());
        this.map = new HashMap<>();
    }

    @Override
    protected void loadLine(String line) {
        //String r = line.replaceAll("\"", "");//no need, as it isn't a Yaml file anymore
        try {
            String[] a = line.split(": ", 2);
            String message = AlixFormatter.translateColors(a[1]); //outdated explanation - AlixFormatter instead of AlixUtils because of class initialization issues
            map.put(a[0], removeFrontSpace(message));
        } catch (Exception e) {
            throw new RuntimeException("An error was caught whilst initializing messages on line: " + line, e);
        }
    }

/*    public void save() throws IOException {//TODO: Fix " in Yaml, if you want to save this shit
        Map<String, String> map = new HashMap<>(this.map);
        map.replaceAll((k, v) -> v + ' ');
        super.save(map);
    }*/

    public Map<String, String> getMap() {
        return map;
    }

    private static String removeFrontSpace(String message) {//often made by accident
        return message.charAt(0) == ' ' ? message.substring(1) : message;
    }

    private static File findFile() {
        String name = AlixCommonMain.MAIN_CLASS_INSTANCE.getEngineParams().messagesFileName();
        File f = AlixFileManager.getPluginFile(name, FileType.CONFIG);
        if (f.exists()) return f;
/*        String lang = JavaUtils.pluginLanguage.getShortcut();
        File f2 = FileManager.getPluginFile("messages_" + lang + ".txt");
        if (f2.exists()) return f2;*/
        //Main.debug("Unable to find this plugin's messages.txt file. Generating a new one.");
        return AlixFileManager.createPluginFile(name, FileType.CONFIG);
    }
}