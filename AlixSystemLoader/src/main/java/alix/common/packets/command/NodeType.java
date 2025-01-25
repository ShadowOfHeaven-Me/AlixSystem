package alix.common.packets.command;

//https://wiki.vg/Command_Data#Flags
public enum NodeType {

    ROOT(0x00),
    LITERAL(0x01),
    ARGUMENT(0x02),
    NOT_USED(0x03);

    private final int bitMask;

    NodeType(int mask) {
        this.bitMask = mask;
    }

    public int getBitMask() {
        return bitMask;
    }
}