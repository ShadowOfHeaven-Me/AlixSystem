package alix.common.antibot.firewall;

public final class FireWallEntry {

    private final String algorithmId;

    public FireWallEntry(String algorithmId) {
        this.algorithmId = algorithmId;
    }

    @Override
    public String toString() {
        return algorithmId;
    }
}