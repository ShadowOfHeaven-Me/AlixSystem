package alix.common.update;

import alix.loaders.bukkit.BukkitAlixMain;
import alix.common.utils.file.FileManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileUpdater {

    private static final char DEFAULT_SPLITERATOR = ':';

    public static void updateFiles() {

        //messages.txt

        File messagesFile = updateFile("messages.txt");

        MessagesFileUpdater.updateFormatting(messagesFile);

        //commands.txt

        updateFile("commands.txt");

        //config.yml

        updateFile("config.yml");

    }

    /**
     * Updates the file - Adds the missing lines and removes the outdated ones, by comparing it to the file compiled with the plugin
     *
     * @param name The plugin's file name
     *
     *
     *             Returns:
     */

    private static File updateFile(String name) {
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
            getWithJarCompiledFile(file, name);//writes the newest info into the file
            return file;//no need to continue, the file must be up to date
        }

        File tempFile = new File(file.getParent(), splitName[0] + "-copy." + splitName[1]); //<file name>-copy.<extension>

        File newestFile = getWithJarCompiledFile(tempFile, name);//temp file is the exact same thing as the 'newest file'

        ensureUpdated(file, newestFile, DEFAULT_SPLITERATOR);

        tempFile.delete();
        return file;
    }

    private static File getWithJarCompiledFile(File copyInto, String s) {
        //tmpdir - tf is this
        try (InputStream in = BukkitAlixMain.class.getClassLoader().getResourceAsStream(s)) {
            Files.copy(in, copyInto.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return copyInto;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getPluginFile(String fileName) {
        String path = BukkitAlixMain.instance.getDataFolder().getAbsolutePath();

        new File(path).mkdirs(); //Creating the folder if absent

        return new File(path + File.separator + fileName);
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

    public static void ensureUpdated(File existingFile, File newestFile, char spliterator) {
        List<String> existingLines = FileManager.getLines(existingFile);
        List<String> newestLines = FileManager.getLines(newestFile);

        //Main.logInfo("Size 1: " + existingLines.size() + " Size 2: " + newestLines.size());

        for (int i = 0; i < newestLines.size(); i++) {//iterating through the newest lines
            String newestLine = newestLines.get(i);

            if (newestLine == null || newestLine.isEmpty()) continue;

            for (String existingLine : existingLines) {//iterating through the existing lines
                if (existingLine == null || existingLine.isEmpty()) continue;// || existingLine.startsWith("#")

                String existingLineStart = existingLine.split(String.valueOf(spliterator))[0];

                String newestLineStart = newestLine.split(String.valueOf(spliterator))[0];

                if ( existingLineStart.equals(newestLineStart)) {//the line exists, so we copy it's config
                    newestLines.set(i, existingLine);
                    //Main.debug("Config copied from '" + newestLine + "' to '" + existingLineStart);
                    break;
                }
            }
        }

        try {
            FileManager.write(existingFile, newestLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
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