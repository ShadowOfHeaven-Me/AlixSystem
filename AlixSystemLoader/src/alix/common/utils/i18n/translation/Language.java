package alix.common.utils.i18n.translation;

public enum Language {

    UNIVERSAL(null),
    UNKNOWN(""),
    AFRIKAANS("af"),
    ALBANIAN("sq"),
    AMHARIC("am"),
    ARABIC("ar"),
    ARMENIAN("hy"),
    AZERBAIJANI("az"),
    BASQUE("eu"),
    BELARUSIAN("be"),
    BENGALI("bn"),
    BOSNIAN("bs"),
    BULGARIAN("bg"),
    BURMESE("my"),
    CATALAN("ca"),
    CEBUANO("ceb"),
    CHICHEWA("ny"),
    CHINESE_SIMPLIFIED("zh-CN"),
    CHINESE_TRADITIONAL("zh-TW"),
    CORSICAN("co"),
    CROATIAN("hr"),
    CZECH("cs"),
    DANISH("da"),
    DUTCH("nl"),
    ENGLISH("en"),
    ESPERANTO("eo"),
    ESTONIAN("et"),
    FILIPINO("tl"),
    FINNISH("fi"),
    FRENCH("fr"),
    FRISIAN("fy"),
    GALICIAN("gl"),
    GEORGIAN("ka"),
    GERMAN("de"),
    GREEK("el"),
    GUJARATI("gu"),
    HAITIAN_CREOLE("ht"),
    HAUSA("ha"),
    HAWAIIAN("haw"),
    HEBREW("he"),
    HINDI("hi"),
    HMONG("hmn"),
    HUNGARIAN("hu"),
    ICELANDIC("is"),
    IGBO("ig"),
    INDONESIAN("id"),
    IRISH("ga"),
    ITALIAN("it"),
    JAPANESE("ja"),
    JAVANESE("jv"),
    KANNADA("kn"),
    KAZAKH("kk"),
    KHMER("km"),
    KINYARWANDA("rw"),
    KOREAN("ko"),
    KURDISH("ku"),
    KYRGYZ("ky"),
    LAO("lo"),
    LATIN("la"),
    LATVIAN("lv"),
    LITHUANIAN("lt"),
    LUXEMBOURGISH("lb"),
    MACEDONIAN("mk"),
    MALAGASY("mg"),
    MALAY("ms"),
    MALAYALAM("ml"),
    MALTESE("mt"),
    MAORI("mi"),
    MARATHI("mr"),
    MONGOLIAN("mn"),
    NEPALI("ne"),
    NORWEGIAN("no"),
    NYANJA("ny"),
    ODIA("or"),
    PASHTO("ps"),
    PERSIAN("fa"),
    POLISH("pl"),
    PORTUGUESE("pt"),
    PUNJABI("pa"),
    ROMANIAN("ro"),
    RUSSIAN("ru"),
    SAMOAN("sm"),
    SCOTS_GAELIC("gd"),
    SERBIAN("sr"),
    SESOTHO("st"),
    SHONA("sn"),
    SINDHI("sd"),
    SINHALA("si"),
    SLOVAK("sk");

    private final String shortcut;

    Language(String shortcut) {
        this.shortcut = shortcut;
    }

    public final String getShortcut() {
        return shortcut;
    }

    public static Language getByShortcut(String shortcut) throws IllegalArgumentException {
        if (shortcut == null) return UNIVERSAL;
        String lowerCasedShortcut = shortcut.toLowerCase();
        for (Language l : values())
            if (lowerCasedShortcut.equals(l.shortcut))
                return l;
        return UNKNOWN;
        //throw new IllegalArgumentException("Unsupported language shortcut '" + shortcut + "'!");
    }

    public static Language getByName(String name) throws IllegalArgumentException {
        if (name == null) return UNIVERSAL;
        String upperCasedName = name.toUpperCase();
        for (Language l : values())
            if (upperCasedName.equals(l.name()))
                return l;
        return UNKNOWN;
        //throw new IllegalArgumentException("Unsupported language type '" + name + "'!");
    }

    public static Language getMostSimilar(String lang) {
        if (lang == null) return UNIVERSAL;
        String upperCasedLang = lang.toUpperCase();
        for (Language l : values())
            if (l.name().startsWith(upperCasedLang))
                return l;
        return UNKNOWN;
    }
}
