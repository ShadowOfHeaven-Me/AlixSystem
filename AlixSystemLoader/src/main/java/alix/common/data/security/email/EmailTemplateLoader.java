package alix.common.data.security.email;

import alix.common.AlixCommonMain;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

//Loads custom HTML email templates from the plugin's data folder, so operators can customize outgoing
//verification emails instead of using the plain built-in text body.
final class EmailTemplateLoader {

    private static final String TEMPLATES_FOLDER_NAME = "email-templates";
    //e.g. email-templates/images/logo.png
    private static final String IMAGES_FOLDER_NAME = "images";

    //Creates the folders on startup instead of lazily on first load()/loadImageFile() call - otherwise, for
    //an operator who hadn't sent a custom-templated email yet, the folder never appeared at all.
    static void ensureFoldersExist() {
        new File(templatesFolder(), IMAGES_FOLDER_NAME).mkdirs();
    }

    /**
     * @param fileName name of the HTML file, relative to the plugin's "email-templates" folder
     * @return the file's content, or empty if the file does not exist or could not be read (in which case the caller should fall back to the default template)
     */
    static Optional<String> load(String fileName) {
        File templatesFolder = templatesFolder();
        File file = new File(templatesFolder, fileName);

        if (!isInside(templatesFolder, file, fileName)) return Optional.empty();

        if (!file.exists() || !file.isFile()) {
            AlixCommonMain.logWarning("Custom email template '" + fileName + "' was not found in the '" + TEMPLATES_FOLDER_NAME + "' folder! Falling back to the default template.");
            return Optional.empty();
        }

        try {
            return Optional.of(Files.readString(file.toPath(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            AlixCommonMain.logWarning("Could not read custom email template '" + fileName + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * @param fileName name of the image file, relative to the plugin's "email-templates/images" folder (referenced from a template via e.g. {@code <img src="cid:logo.png">})
     * @return the image file, or empty if it does not exist
     */
    static Optional<File> loadImageFile(String fileName) {
        File folder = new File(templatesFolder(), IMAGES_FOLDER_NAME);
        folder.mkdirs();

        File file = new File(folder, fileName);
        if (!isInside(folder, file, fileName)) return Optional.empty();

        return (file.exists() && file.isFile()) ? Optional.of(file) : Optional.empty();
    }

    //Not currently reachable with a traversal-shaped name (the only caller's CID pattern already forbids
    //'/'/'\' and rejects ".." substrings) but load() and loadImageFile() both resolve an operator-set file
    //name under a folder, so both get the same defense rather than relying solely on the caller's filtering.
    private static boolean isInside(File folder, File file, String fileName) {
        try {
            String canonicalFolder = folder.getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (canonicalFile.equals(canonicalFolder) || canonicalFile.startsWith(canonicalFolder + File.separator))
                return true;
            AlixCommonMain.logWarning("'" + fileName + "' resolves outside the '" + folder.getName() + "' folder - refusing to load it!");
            return false;
        } catch (IOException e) {
            AlixCommonMain.logWarning("Could not resolve '" + fileName + "': " + e.getMessage());
            return false;
        }
    }

    private static File templatesFolder() {
        File dataFolder = AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toAbsolutePath().toFile();
        File folder = new File(dataFolder, TEMPLATES_FOLDER_NAME);
        folder.mkdirs();
        return folder;
    }

    private EmailTemplateLoader() {
    }
}
