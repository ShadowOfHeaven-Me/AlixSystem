package alix.common.antibot.firewall;

public final class FireWallEntry {

    private final String algorithmId;
    private final boolean isBuiltIn;

    public FireWallEntry(String algorithmId) {
        this.algorithmId = algorithmId;
        this.isBuiltIn = algorithmId == null;
    }

    @Override
    public String toString() {
        return algorithmId;
    }

    public boolean isNotBuiltIn() {
        return !isBuiltIn;
    }
}