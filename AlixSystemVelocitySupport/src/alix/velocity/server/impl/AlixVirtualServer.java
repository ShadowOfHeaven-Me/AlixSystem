package alix.velocity.server.impl;

import alix.loaders.velocity.VelocityAlixMain;
import com.velocitypowered.api.network.ProtocolVersion;
import net.elytrium.limboapi.LimboAPI;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.player.GameMode;
import net.elytrium.limboapi.api.protocol.PacketDirection;
import net.elytrium.limboapi.api.protocol.packets.PacketMapping;
import net.elytrium.limboapi.protocol.packets.s2c.MapDataPacket;
import net.elytrium.limboapi.server.world.SimpleBlock;
import net.elytrium.limboapi.server.world.SimpleWorld;
import net.elytrium.limboapi.thirdparty.org.bstats.velocity.Metrics;

import java.lang.reflect.Constructor;

public final class AlixVirtualServer {

    private final LimboAPI api;
    private final Limbo limbo;

    public AlixVirtualServer(VelocityAlixMain i) {
        this.api = constructAPI0(i);
        this.limbo = this.api.createLimbo(this.constructWorld0()).setName("AlixAuth").setGameMode(GameMode.ADVENTURE)
                .setSimulationDistance(0).setViewDistance(0).setWorldTime(0).setShouldUpdateTags(false)
                .registerPacket(PacketDirection.CLIENTBOUND, MapDataPacket.class, MapDataPacket::new, new PacketMapping[]{
                        new PacketMapping(0x25, ProtocolVersion.MINECRAFT_1_16_4, true),
                        new PacketMapping(0x27, ProtocolVersion.MINECRAFT_1_17, true),
                        new PacketMapping(0x27, ProtocolVersion.MINECRAFT_1_17_1, true),
                        new PacketMapping(0x27, ProtocolVersion.MINECRAFT_1_18, true),
                        new PacketMapping(0x27, ProtocolVersion.MINECRAFT_1_18_2, true),
                        new PacketMapping(0x24, ProtocolVersion.MINECRAFT_1_19, true),
                        new PacketMapping(0x26, ProtocolVersion.MINECRAFT_1_19_1, true),
                        new PacketMapping(0x25, ProtocolVersion.MINECRAFT_1_19_3, true),
                        new PacketMapping(0x29, ProtocolVersion.MINECRAFT_1_19_4, true),
                        new PacketMapping(0x2A, ProtocolVersion.MINECRAFT_1_20, true)
                });
    }

    public Limbo getLimbo() {
        return limbo;
    }

    public LimboAPI getAPI() {
        return api;
    }

    public void onProxyInit() {
        this.api.onProxyInitialization(null);
    }

    private VirtualWorld constructWorld0() {
        VirtualWorld world = new SimpleWorld(Dimension.OVERWORLD, 0.5, 2, 0.5, 0, 45);
        SimpleBlock barrier = SimpleBlock.solid((short) 166);
        for (int x = -1; x <= 1; x++) {
            for (int y = 1; y <= 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    boolean air = (y == 2 || y == 3) && x == 0 && z == 0;

                    world.setBlock(x, y, z, air ? SimpleBlock.AIR : barrier);
                }
            }
        }
        return world;
    }

    private static LimboAPI constructAPI0(VelocityAlixMain i) {
        try {
            Constructor<Metrics.Factory> cons = (Constructor<Metrics.Factory>) Metrics.Factory.class.getDeclaredConstructors()[0];
            cons.setAccessible(true);
            Metrics.Factory metricsFactory = cons.newInstance(i.getServer(), i.getLogger(), i.getDataDirectory());

            return new LimboAPI(i.getLogger(), i.getServer(), metricsFactory, i.getDataDirectory());
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
