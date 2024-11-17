package nanolimbo.alix.protocol.packets.play;

import nanolimbo.alix.protocol.ByteMessage;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.registry.Version;

public class PacketGameEvent implements PacketOut {

    private byte type;
    private float value;

    public void setType(byte type) {
        this.type = type;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void encode(ByteMessage msg, Version version) {
        msg.writeByte(type);
        msg.writeFloat(value);
    }
}
