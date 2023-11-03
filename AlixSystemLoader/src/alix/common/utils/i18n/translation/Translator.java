/*
package shadow.utils.i18n.translation;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;

public class Translator {

    protected final TranslationLanguage sourceLanguage, targetLanguage;
    private final TranslateOptions translator;

    public Translator(String sourceLanguage, String targetLanguage) {
        this.sourceLanguage = TranslationLanguage.getByShortcut(sourceLanguage);
        this.targetLanguage = TranslationLanguage.getByShortcut(targetLanguage);
        translator = TranslateOptions.newBuilder().setTargetLanguage(targetLanguage).build();
    }

    public String translateText(String text) {
        String fromLang = sourceLanguage.getShortcut();
        String toLang = targetLanguage.getShortcut();
        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            // Supported Locations: `global`, [glossary location], or [model location]
            // Glossaries must be hosted in `us-central1`
            // Custom Models must use the same location as your model. (us-central1)
            LocationName parent = LocationName.of(projectId, "global");

            // Supported Mime Types: https://cloud.google.com/translate/docs/supported-formats
            TranslateTextRequest request =
                    TranslateTextRequest.newBuilder()
                            .setParent(parent.toString())
                            .setMimeType("text/plain")
                            .setTargetLanguageCode(targetLanguage)
                            .addContents(text)
                            .build();

            TranslateTextResponse response = client.translateText(request);
        }
    }
}*/
