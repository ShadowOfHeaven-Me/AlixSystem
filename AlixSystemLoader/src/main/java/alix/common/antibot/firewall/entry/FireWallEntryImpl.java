package alix.common.antibot.firewall.entry;

final class FireWallEntryImpl implements FireWallEntry {

    private final String algorithmId;
    private final boolean isBuiltIn;
    private final long timeoutAt;

    FireWallEntryImpl(String algorithmId, long timeoutAt) {
        this.algorithmId = algorithmId;
        this.isBuiltIn = algorithmId == null;
        this.timeoutAt = timeoutAt;
    }

    @Override
    public String toString() {
        return algorithmId + DELIMITER +
               this.timeoutAt;
    }

    @Override
    public long timeoutAt() {
        return this.timeoutAt;
    }
}