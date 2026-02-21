package alix.common.data.fingerprinting;

public final class FingerprintBuilder {

    public static final int LEN = 24;
    int value;
    private int idx;

    public FingerprintBuilder() {
    }

    public Fingerprint getFingerprint() {
        return new Fingerprint(this);
    }

    /*public boolean receive(PacketPlayInResourcePackResponse response) {
        Integer idx = FingerprintManager.getIndex(response.wrapper().getPackId());
        if (idx == null)
            throw NettySafety.INVALID_UUID_RESP;

        return this.set(idx, )
    }*/

    public boolean set(int idx, boolean val) {
        this.value |= (val ? 1 : 0) << idx;
        return ++this.idx == LEN;
    }

    //true if completed
    /*public boolean set(boolean val) {
        this.value |= (val ? 1 : 0) << (this.idx++);
        return this.idx == LEN;
    }*/
}