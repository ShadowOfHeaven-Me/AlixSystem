/*
package shadow.systems.login.autoin.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import shadow.Main;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.systems.login.autoin.ProtocolSupportRegistry;

public class ProtocolLibEvents implements ProtocolSupportRegistry {

    private final ProtocolManager protocolManager;
    private final PacketListener listener;

    public ProtocolLibEvents() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.listener = new PacketAdapter(PacketAdapter.params().plugin(Main.plugin).gamePhase(GamePhase.LOGIN).types(PacketType.Login.Client.ENCRYPTION_BEGIN)) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                //PremiumAutoIn.add(event.getPlayer().getEntityId());
            }
        };
    }


    @Override
    public void register() {
        protocolManager.addPacketListener(listener);
    }

    @Override
    public void unregister() {
        protocolManager.removePacketListener(listener);
    }
}*/
