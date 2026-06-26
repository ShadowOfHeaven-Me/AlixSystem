package alix.common.antibot.firewall.entry;

public interface FireWallEntry {

    default boolean isNotBuiltIn() {
        return this != BUILT_IN; //!(this instanceof BuiltInEntryImpl);
    }

    default boolean shouldSave() {
        long timeoutAt = this.timeoutAt();
        return this.isNotBuiltIn() && (timeoutAt <= 0 || System.currentTimeMillis() < timeoutAt);
    }

    long timeoutAt();

    static FireWallEntry from(String message, long timeoutAt) {
        return new FireWallEntryImpl(message, timeoutAt);
    }

    FireWallEntry BUILT_IN = new BuiltInEntryImpl();

    String DELIMITER = "|";
}