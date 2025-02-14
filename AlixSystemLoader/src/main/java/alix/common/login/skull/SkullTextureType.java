package alix.common.login.skull;

public enum SkullTextureType {

    WOODEN_SKULL,
    CHAT_STONE_SKULL,
    PLUSH_SKULL,
    QUARTZ_SKULL;

    public static boolean isSkull(String s) {
        try {
            valueOf(s.toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static SkullTextureType parseSafe(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (Exception e) {
            return WOODEN_SKULL;
        }
    }
}