package alix.velocity.systems.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.proxy.VelocityServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class PacketEventsManager {

    public static void init(PluginContainer pluginContainer, VelocityServer server, Logger logger, Path dataDirectory) {
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, pluginContainer, logger, dataDirectory));
        PacketEvents.getAPI().init();
    }

    public static void register() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventListener());
    }
}