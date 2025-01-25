package alix.common.packets.command;

//https://wiki.vg/Command_Data#Flags
public enum NodeFlag {

    IS_EXECUTABLE(0x04),
    HAS_REDIRECT(0x08),
    HAS_SUGGESTIONS_TYPE(0x10);

    private final int bitMask;

    NodeFlag(int mask) {
        this.bitMask = mask;
    }

    public int getBitMask() {
        return bitMask;
    }
}