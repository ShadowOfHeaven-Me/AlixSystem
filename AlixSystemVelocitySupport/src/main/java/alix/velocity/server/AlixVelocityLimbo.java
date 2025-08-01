package alix.velocity.server;

import alix.common.utils.floodgate.GeyserUtil;
import alix.velocity.Main;
import alix.velocity.server.impl.VelocityLimboIntegration;
import alix.velocity.utils.user.UserManager;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.netty.channel.Channel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.handlers.DummyHandler;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Connections;
import ua.nanit.limbo.server.LimboServer;

import static com.velocitypowered.proxy.network.Connections.READ_TIMEOUT;

public final class AlixVelocityLimbo {

    private static final String VELOCITY_TIMEOUT = "---timeout";//keep the 3 dashes
    private static final LimboServer limbo = NanoLimbo.load(new VelocityLimboIntegration());
    public static final Connections LIMBO_CONNECTIONS = limbo.getConnections();
    //private static final Map<ConnectedPlayer, Continuation> continuationMap = new ConcurrentHashMap<>();

    public static void initChannel(Channel channel) {
        limbo.getClientChannelInitializer().initChannel(channel);
    }

    public static void initAfterLoginSuccess(ConnectedPlayer player, Continuation continuation, GeyserUtil geyserUtil) {
        Channel channel = player.getConnection().getChannel();
        Version version = Version.of(player.getProtocolVersion().getProtocol());

        channel.pipeline().replace(READ_TIMEOUT, VELOCITY_TIMEOUT, DummyHandler.HANDLER);

        limbo.getClientChannelInitializer().initAfterLoginSuccess(channel, version, player.getUsername(), connection -> {
            channel.pipeline().replace(VELOCITY_TIMEOUT, READ_TIMEOUT, new ReadTimeoutHandler(Main.PLUGIN.getServer().getConfiguration().getReadTimeout()));
            UserManager.add(player);
            continuation.resume();


            /*Runnable joinBackend = () -> {
                channel.pipeline().replace(VELOCITY_TIMEOUT, Connections.READ_TIMEOUT, new ReadTimeoutHandler(Main.PLUGIN.getServer().getConfiguration().getReadTimeout()));
                continuation.resume();
            };

            if (!connection.hasConfigPhase()) {
                joinBackend.run();
                return;
            }
            channel.eventLoop().execute(() -> {
                connection.writeAndFlushPacket(PacketSnapshots.RECONFIGURE);

                AlixScheduler.async(joinBackend);
            });*/
        }, geyserUtil);

        /*channel.eventLoop().schedule(() -> {
            ChannelIdentifier brandChannel = MinecraftChannelIdentifier.from("minecraft:brand");
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("YourBrandHere"); // Replace with your brand info
            player.sendPluginMessage(brandChannel, out.toByteArray());
            Log.error("SENT BRANDDDD");
        }, 5, TimeUnit.SECONDS);*/
    }

    public static void init() {
    }
}