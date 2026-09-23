package alix.common.data.security.email;

import alix.common.AlixCommonMain;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Loads custom HTML email templates from the plugin's data folder, allowing operators to fully
 * customize the appearance of outgoing verification emails (their own branding, embedded images, etc.)
 * instead of using the plain built-in text body.
 */
final class EmailTemplateLoader {

    private static final String TEMPLATES_FOLDER_NAME = "email-templates";
    //images referenced by custom templates (e.g. a logo) live in a subfolder of the templates folder, e.g. email-templates/images/logo.png
    private static final String IMAGES_FOLDER_NAME = "images";

    /**
     * Proactively creates the "email-templates" folder (and its "images" subfolder) so operators can find
     * it and drop a template in right after installing/starting the plugin, instead of it only appearing
     * as a side effect the first time {@link #load(String)}/{@link #loadImageFile(String)} happen to run -
     * which, since both are only reached while actually sending a verification email with
     * 'custom-verify-email-template' already set to a real file, previously meant the folder never
     * appeared at all for an operator who hadn't gotten that far yet (the exact case reported live: the
     * folder simply never showed up because no email had ever been sent with a custom template configured).
     * Called once from {@link EmailConfig}'s constructor, i.e. on every plugin startup.
     */
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

        //'fileName' is the operator-set 'custom-verify-email-template' value in email-config.yml, not
        //attacker-controlled - but resolving it outside the templates folder (e.g. "../../secrets.yml", or
        //an absolute path) would still let it read an arbitrary file on the host and mail its contents back
        //as the "verification email" body, so it's rejected the same as any other path-traversal input
        //would be. Canonicalizing (rather than a simple ".." substring check) also catches an absolute path.
        try {
            String canonicalTemplatesFolder = templatesFolder.getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.equals(canonicalTemplatesFolder) && !canonicalFile.startsWith(canonicalTemplatesFolder + File.separator)) {
                AlixCommonMain.logWarning("Custom email template '" + fileName + "' resolves outside the '" + TEMPLATES_FOLDER_NAME + "' folder - refusing to load it! Falling back to the default template.");
                return Optional.empty();
            }
        } catch (IOException e) {
            AlixCommonMain.logWarning("Could not resolve custom email template '" + fileName + "': " + e.getMessage());
            return Optional.empty();
        }

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
        return (file.exists() && file.isFile()) ? Optional.of(file) : Optional.empty();
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
