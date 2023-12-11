package shadow.systems.executors.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import shadow.Main;
import shadow.systems.login.Verifications;
import shadow.systems.login.result.LoginVerdictManager;
import shadow.utils.objects.savable.data.gui.AlixGui;
import shadow.utils.users.offline.UnverifiedUser;

public final class PacketEventsListener extends PacketListenerAbstract {

    public PacketEventsListener() {
        super(PacketListenerPriority.LOWEST);
        PacketEvents.getAPI().getEventManager().registerListener(this);
        Main.logInfo("Implemented async GUI handling thanks to PacketEvents");
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            UnverifiedUser user = Verifications.get(event.getUser().getUUID());
            if (user == null) return;
            if (!user.isGUIInitialized()) {//the player didn't click an alix inventory
                event.setCancelled(true);//cancel the packet
                return;//stop further processing
            }
            WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            AlixGui.onAsyncClick(user, packet.getSlot());
            event.setCancelled(true);//cancelling the event to discourage further processing by the server
        }
    }

/*    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
            WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);

        }
    }*/
}