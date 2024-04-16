package shadow.utils.objects.packet;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;

public interface PacketProcessor {

    void onPacketReceive(PacketPlayReceiveEvent event);

    void onPacketSend(PacketPlaySendEvent event);

}