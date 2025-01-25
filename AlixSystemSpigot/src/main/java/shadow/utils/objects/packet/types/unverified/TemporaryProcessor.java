package shadow.utils.objects.packet.types.unverified;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

public final class TemporaryProcessor {

    private volatile boolean login;

    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Login.Client.LOGIN_SUCCESS_ACK) return;
        //Main.logInfo("IN: " + event.getPacketType().getName() + " " + (login ? "CANCELLED" : ""));

        //if (login) event.setCancelled(true);
    }


    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Login.Server.LOGIN_SUCCESS) {
            login = true;
            return;
        }
        if (event.getPacketType() == PacketType.Configuration.Server.REGISTRY_DATA ||
                event.getPacketType() == PacketType.Configuration.Server.UPDATE_TAGS) {
            return;
        }
        //Main.logInfo("OUT: " + event.getPacketType().getName() + " " + (login ? "CANCELLED" : ""));
        //if (login) event.setCancelled(true);
    }
}