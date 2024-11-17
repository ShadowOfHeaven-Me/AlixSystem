package shadow.utils.objects.packet;

import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import alix.libs.com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;

public interface PacketProcessor {

    void onPacketReceive(PacketPlayReceiveEvent event);

    void onPacketSend(PacketPlaySendEvent event);

}