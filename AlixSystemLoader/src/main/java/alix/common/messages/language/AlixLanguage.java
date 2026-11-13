package alix.common.messages.language;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AlixLanguage {

    ENGLISH("en"),
    CZECH("cs"),
    POLISH("pl");

    public static final Set<String> SUPPORTED_LANGUAGES = Arrays.stream(values()).map(AlixLanguage::getCode).collect(Collectors.toSet());
    public static final String DEFAULT_LANGUAGE = ENGLISH.code;
    @Getter
    private final String code;

    AlixLanguage(String code) {
        this.code = code;
    }

    public static boolean isSupported(String code) {
        return SUPPORTED_LANGUAGES.contains(code);
    }
}