/*
package shadow.utils.i18n.translation;

import shadow.Main;
import shadow.utils.main.JavaUtils;

public class AlixTranslator extends Translator {

    private static final Translator instance;

    public AlixTranslator(String sourceLanguage, String targetLanguage) {
        super(sourceLanguage, targetLanguage == null ? "en" : targetLanguage);
        if (targetLanguage == null) Main.logInfo("Target language cannot be null! Using english as default.");
    }

    public static String translate(String t) {
        return instance.translateText(t);
    }

    public static void init() {
        TranslationLanguage l = instance.sourceLanguage;
        Main.logInfo("Using " + JavaUtils.firstCharToUpperCase(l.name().toLowerCase()) + " (" + l.getShortcut().toUpperCase() + ") as the translation base for the plugin language translator.");
    }

    static {
        String language = Main.config.getString("language");
        instance = new AlixTranslator("pl", language.toLowerCase());
    }
}*/
