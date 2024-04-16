package alix.common.utils.file.update;

import alix.common.environment.ServerEnvironment;
import alix.common.utils.file.AlixFileManager;
import alix.loaders.bukkit.BukkitAlixMain;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class FileUpdater {

    private static final char DEFAULT_SPLITERATOR = ':';

    public static void updateFiles() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                //messages.txt

                File messagesFile = updateFile("messages.txt");
                MessagesFileUpdater.updateFormatting(messagesFile);

                //commands.txt

                updateFile("commands.txt", Validation.VALIDATE_HASHTAG_START);

                //config.yml

                updateFile("config.yml", Validation.VALIDATE_TRIMMED_DASH_START);
                break;
            case VELOCITY:
                updateFile("config.yml", Validation.VALIDATE_TRIMMED_DASH_START);
                break;
        }
    }

    /**
     * Updates the file - Adds the missing lines and removes the outdated ones, by comparing it to the file compiled with the plugin
     *
     * @param name     The plugin's file name
     * @param validate Determines when the lines should still be considered valid,
     *                 and not outdated, thus suggesting a config and not being overridden
     *                 <p>
     *                 Returns: The update file's version
     */

    private static File updateFile(String name, Validation... validate) {
        String[] splitName = name.split("\\.");

        if (splitName.length != 2)
            throw new ExceptionInInitializerError("File name split length is different from 2! " + name + " " + splitName.length);

        File file = new File(BukkitAlixMain.instance.getDataFolder(), name);

        if (!file.exists()) {
            try {
                file.createNewFile();//creates the file
            } catch (IOException e) {
                e.printStackTrace();
            }
            AlixFileManager.writeJarCompiledFileIntoDest(file, name);//writes the newest info into the file
            return file;//no need to continue, the file must be up to date
        }

        File tempFile = new File(file.getParent(), splitName[0] + "-copy." + splitName[1]); //<file name>-copy.<extension>

        File newestFile = AlixFileManager.writeJarCompiledFileIntoDest(tempFile, name);//temp file is the exact same thing as the 'newest file'

        ensureUpdated(file, newestFile, DEFAULT_SPLITERATOR, Arrays.asList(validate));

        tempFile.delete();
        return file;
    }

/*    private static File getPluginFile(String fileName) {
        String path = BukkitAlixMain.instance.getDataFolder().getAbsolutePath();

        new File(path).mkdirs(); //Creating the folder if absent

        return new File(path + File.separator + fileName);
    }*/

    private enum Validation {

        //if "firewall: true" is the line, "#firewall: true" should also be validated and the line should be considered as present
        VALIDATE_HASHTAG_START,
        //all lines starting with a dash "-" are considered a valid method of configuring a parameter
        VALIDATE_TRIMMED_DASH_START

    }

    /*
        File messagesFile = Messages.getFile().getFile();
        update(messagesFile, () -> MessagesFile.createFile("messages.txt"), ':');*/

/*    String originalName = oldFile.getName();
    String extension = originalName.substring(originalName.lastIndexOf('.'));
    String updatedName = originalName.replace(extension, "-copy" + extension);
    File modifiedOldFile = new File(oldFile.getParent(), updatedName);
    File newFile = new File(oldFile.getParent(), originalName);

    // Rename the old file to the modified name
        if (!oldFile.renameTo(modifiedOldFile)) {
        System.out.println("Failed to rename the file.");
        return;
    }

    // Create the new file using the runnable
        createTheNewFile.run();

    // Perform the update operation on the modified old file and the new file
    ensureUpdated(modifiedOldFile, newFile, spliterator);

    // Delete the new file
        if (newFile.exists()) {
        newFile.delete();
    }

    // Rename the modified old file back to the original name
        if (!modifiedOldFile.renameTo(oldFile)) {
        System.out.println("Failed to revert the file name.");
    }*/

/*    public static void update(File existingFile, Runnable createCopiedFile, char spliterator) {

        String originalName = existingFile.getName();

        String[] splitName = originalName.split("\\.");

        String extension = splitName[1];

        String copyName = splitName[0] + "-copy." + extension;

        createCopiedFile.run();
        File newestFile = new File(existingFile.getParent(), copyName);


        ensureUpdated(existingFile, newestFile, spliterator);

        newestFile.delete();
    }*/

/*    public static void updateFile(File existingFile, File newestFile, char spliterator) { I'll add it in the future
        File configFile = new File(Main.instance.getDataFolder(), "config.yml");

        File temp = new File(configFile.getParent(), "config-copy.yml");

        File newestConfigFile = FileManager.getWithJarCompiledFile(temp, "config.yml");

        ensureUpdated(configFile, newestConfigFile, spliterator);

        temp.delete();

    }*/


    /**
     * Ensures the existing file has all the parameters of the newest file, whilst keeping the user configuration
     *
     * @param existingFile The currently existing file
     * @param newestFile   The file containing the newest lines
     * @param spliterator  The lines are checked from the start of the line until the spliterator char,
     *                     to ensure that line exists, as the rest of the text after the spliterator
     *                     is customizable by the user (usually it's ':', like in the config.yml file)
     */

    //Tbh I don't exactly know why the dash ('-') updater suddenly started working, but let's just leave it at that
    //It's not a face, it's a quoted character in braces
    public static void ensureUpdated(File existingFile, File newestFile, char spliterator, List<Validation> validate) {
        List<String> existingLines = AlixFileManager.getLines(existingFile);
        List<String> newestLines = AlixFileManager.getLines(newestFile);

        boolean isHashtagStartValid = validate.contains(Validation.VALIDATE_HASHTAG_START);
        boolean isTrimmedDashStartValid = validate.contains(Validation.VALIDATE_TRIMMED_DASH_START);

        //Main.logInfo("Size 1: " + existingLines.size() + " Size 2: " + newestLines.size());


        String splitWith = String.valueOf(spliterator);

        Map<String, List<String>> map = new HashMap<>();
        //List<Integer> copiedIndexes = new ArrayList<>();
        String lastParam = null;


        existing:
        for (int j = 0; j < existingLines.size(); j++) {//iterating through the existing lines
            String existingLine = existingLines.get(j);

            for (int i = 0; i < newestLines.size(); i++) {//iterating through the newest lines
                String newestLine = newestLines.get(i);

                //if (newestLine.isEmpty()) continue;

                //if (existingLine.isEmpty()) continue;

                String existingLineStart = existingLine.split(splitWith)[0].trim();//just trim this bad boi
                String newestLineStart = newestLine.split(splitWith)[0];

                if (isTrimmedDashStartValid) {
                    /*if (existingLineStart.equals("after-login-commands")) {
                        AlixCommonMain.logError("XEEEEEEEEEEEEEEEEEEEEEEEEEES " + !existingLineStart.isEmpty() + " " + !existingLineStart.startsWith("#") + " " + !existingLineStart.startsWith("-"));
                    }*/

                    //AlixCommonMain.logWarning("ALL GIVEN " + existingLineStart);

                    if (!existingLineStart.isEmpty() && !existingLineStart.startsWith("#") && !existingLineStart.startsWith("-")) {//assume it's a param, since it's not empty, not a comment nor an enumeration
                        lastParam = existingLineStart;
                        map.computeIfAbsent(lastParam, s -> new ArrayList<>());
                    }

                    if (existingLineStart.startsWith("-")) {
                        if (lastParam == null) break;
                        List<String> list = map.get(lastParam);//get the non-null list of parameters
                        if (!list.isEmpty()) break;//continue the outer loop if it was already initialized

                        //Initialize the parameter list
                        list.add(existingLine);
                        for (int k = j + 1; k < existingLines.size(); k++) {//iterate until you find the entirety of the given list
                            String line = existingLines.get(k);
                            if (line.isEmpty()) continue;
                            if (!line.trim().startsWith("-")) break;
                            list.add(line);
                        }
                        continue existing;//onto the next existing line, I dunno exactly why exactly, but the code breaks without this
                    }
                }

                if (isHashtagStartValid && removeHashtagStart(existingLineStart).equals(removeHashtagStart(newestLineStart))//the line exists with hashtag start ignore config
                        || existingLineStart.equals(newestLineStart)) {//the line exists
                    newestLines.set(i, existingLine);//we copy the line's config after confirming it's existence
                    break;
                }
            }
        }

        //AlixCommonMain.logInfo("MIIIIIIIII " + map.values());

        if (!map.isEmpty()) {
            for (int i = 0; i < newestLines.size(); i++) {
                String newestLine = newestLines.get(i);
                String newestLineStart = newestLine.split(splitWith)[0];
                List<String> list = map.get(newestLineStart);
                if (list != null) {
                    newestLines.addAll(i + 1, list);//since 'i' is the index of the list header we have to add 1 to have the list's parameters added below it
                    i += list.size();//skip the indexes we know are the list parameters
                }
            }
        }
        /*if (isTrimmedDashStartValid) {
            for (int i = 0; i < newestLines.size(); i++) {
                for (int j = 0; j < existingLines.size(); j++) {
                    String existingLine = existingLines.get(j);
                    String existingLineStart = existingLine.split(String.valueOf(spliterator))[0];

                    if (existingLineStart.trim().startsWith("-")) {
                        newestLines.add(i, existingLine);
                        break;
                    }
                }
            }
        }*/
        //CommonAlixMain.logInfo(

        try {
            if (!existingLines.equals(newestLines)) AlixFileManager.write(existingFile, newestLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String removeHashtagStart(String s) {
        while (!s.isEmpty() && s.charAt(0) == '#') s = s.substring(1);//lazy writing
        return s;
    }

    /*        List<String> existingLines = FileManager.getLines(existingFile);
        List<String> newestLines = FileManager.getLines(newestFile);


        Main.logInfo("Size 1: " + existingLines.size() + " Size 2: " + newestLines.size());

        if (existingLines.size() >= newestLines.size()) return;//Will not work for when something is removed, but whatever, I'll add it later
        List<String> updatedLines = new ArrayList<>(newestLines.size());

        int i1 = 0, i2 = 0;

        while (true) {
            if (i1 == existingLines.size()) {
                updatedLines.addAll(Arrays.asList(Arrays.copyOfRange(newestLines.toArray(new String[0]), i1, newestLines.size())));
                break;
            }
            String existingLine = existingLines.get(i1++);
            String newestLine = newestLines.get(i2++);

            String existingLineStart = existingLine.split(String.valueOf(spliterator))[0];
            String newestLineStart = newestLine.split(String.valueOf(spliterator))[0];

            if (existingLineStart.equals(newestLineStart)) {
                updatedLines.add(existingLine);
            } else {
                updatedLines.add(newestLine);
                i2++;
                Main.logInfo("Added missing: " + newestLineStart);
            }
        }

        try {
            FileManager.write(existingFile, updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

}