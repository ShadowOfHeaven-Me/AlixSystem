package shadow.utils.misc.command;

//https://wiki.vg/Command_Data#Flags
public enum NodeType {

    ROOT(0),
    LITERAL(1),
    ARGUMENT(2),
    NOT_USED(3);

    private final int bitMask;

    NodeType(int mask) {
        this.bitMask = mask;
    }

    public int getBitMask() {
        return bitMask;
    }
}