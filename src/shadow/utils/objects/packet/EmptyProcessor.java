package shadow.utils.objects.packet;

import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;

public final class EmptyProcessor implements PacketProcessor {

    public static final EmptyProcessor INSTANCE = new EmptyProcessor();

    private EmptyProcessor() {
    }

    @Override
    public void onPacketReceive(PacketPlayReceiveEvent event) {
    }

    @Override
    public void onPacketSend(PacketPlaySendEvent event) {
    }
}
