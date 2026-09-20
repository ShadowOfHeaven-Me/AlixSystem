package ua.nanit.limbo.connection;

import alix.common.antibot.algorithms.any.RegisteredConnectionAlgoImpl;
import alix.common.antibot.epoll.Telemetry;
import alix.common.antibot.epoll.TelemetryProfiler;
import alix.common.antibot.firewall.AlgorithmId;
import alix.common.antibot.firewall.FireWallManager;
import alix.common.connection.profiler.ConnectionStage;
import alix.common.connection.profiler.LimboJoinProfiler;
import alix.common.utils.AlixCommonUtils;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.motd.MotdHandler;
import ua.nanit.limbo.integration.PreLoginInfo;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.protocol.packets.configuration.PacketInFinishConfiguration;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketConfigDisconnect;
import ua.nanit.limbo.protocol.packets.login.PacketLoginAcknowledged;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.packets.status.PacketInStatusPing;
import ua.nanit.limbo.protocol.packets.status.PacketStatusRequest;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.server.LimboServer;
import ua.nanit.limbo.server.Log;

public final class PacketHandler {

    private final LimboServer server;

    public PacketHandler(LimboServer server) {
        this.server = server;
    }

    public void handle(ClientConnection conn, PacketHandshake packet) {
        if (Telemetry.ENABLED)
            TelemetryProfiler.PROFILER.onHandshake(conn.getChannel(), packet.getIntention().getId());

        conn.updateVersion(packet.getVersion());
        conn.updateState(packet.getNextState());

        conn.setHandshakePacket(packet);
        this.server.getIntegration().onHandshake(conn, packet);

        LimboJoinProfiler.update(conn.getChannel(), ConnectionStage.HANDSHAKE);
        //conn.setJoinedInfo(packet.getHost(), packet.getPort());

        //Log.debug("Pinged from %s [%s]", conn.getAddress(), conn.getClientVersion().toString());

        /*if (server.getConfig().getInfoForwarding().isLegacy()) {
            String[] split = packet.getHost().split("\00");

            if (split.length == 3 || split.length == 4) {
                conn.setAddress(split[1]);
                conn.getGameProfile().setUuid(UuidUtil.fromString(split[2]));
            } else {
                conn.disconnectLogin("You've enabled player info forwarding. You need to connect with proxy");
            }
        }*//* else if (server.getConfig().getInfoForwarding().isBungeeGuard()) {
            if (!conn.checkBungeeGuardHandshake(packet.getHost())) {
                conn.disconnectLogin("Invalid BungeeGuard token or handshake format");
            }
        }*/
    }

    public void handle(ClientConnection conn, PacketInStatusPing packetInStatusPing) {
        if (!conn.replyingWithCachedMotd) {
            //tf do I do???
            var addr = AlixCommonUtils.getAddress(conn.getChannel());
            FireWallManager.add(addr, AlgorithmId.J1, true);
            conn.close();
            return;
        }

        MotdHandler.sendPong(conn);
    }

    public void handle(ClientConnection conn, PacketStatusRequest packet) {
        if (Telemetry.ENABLED)
            TelemetryProfiler.PROFILER.onStatusRequest(conn.getChannel());

        LimboJoinProfiler.update(conn.getChannel(), ConnectionStage.STATUS_REQUEST);
        RegisteredConnectionAlgoImpl.onLoginStartOrStatusRequest(conn.getChannel(), conn.getAddress());

        if (MotdHandler.sendCachedResponse(conn))
            return;

        conn.getFrameDecoder().stopResendCollection();
        conn.uninjectWithRecoded(packet);
        //Log.error("STATUS REQUEST");
        //conn.sendPacket(new PacketStatusResponse(server));
    }

    /*public void handle(ClientConnection conn, PacketStatusPing packet) {
        conn.sendPacketAndClose(packet);
    }*/

    private void handleLogin1(ClientConnection conn) {
        //conn.ensureFirst();
        conn.getFrameDecoder().releaseCollected();

        boolean success = conn.getDuplexHandler().tryEnableCompression(true, false);
        if (!success && NanoLimbo.debugMode)
            Log.warning("COMPRESS NOT ENABLED");


        /*if (server.getConfig().getInfoForwarding().isModern()) {
            int loginId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
            PacketLoginPluginRequest request = new PacketLoginPluginRequest();

            request.setMessageId(loginId);
            request.setChannel(LimboConstants.VELOCITY_INFO_CHANNEL);
            request.setData(Unpooled.EMPTY_BUFFER);

            conn.setVelocityLoginMessageId(loginId);
            conn.sendPacket(request);
            return;
        }

        if (!server.getConfig().getInfoForwarding().isModern()) {
            conn.getGameProfile().setUsername(packet.getUsername());
            conn.getGameProfile().setUuid(UuidUtil.getOfflineModeUuid(packet.getUsername()));
        }*/

        conn.fireLoginSuccess();
    }

    private void handleLogin0(ClientConnection conn, PacketLoginStart packet) {
        if (Telemetry.ENABLED)
            TelemetryProfiler.PROFILER.onLoginStart(conn.getChannel());
        //Log.error("handleLogin0");
        this.server.getIntegration().onLoginStart(conn, packet, info -> {
            this.handleLoginWithInfo0(conn, packet, info);
        });
    }

    private void handleLoginWithInfo0(ClientConnection conn, PacketLoginStart packet, PreLoginInfo info) {
        LimboJoinProfiler.update(conn.getChannel(), ConnectionStage.VERDICT_REACHED, info.result().toString());

        //Intention hide
        boolean isTransfer = conn.getHandshakePacket().isTransfer();
        if (isTransfer && info.result() == PreLoginResult.CONNECT_TO_MAIN_SERVER)
            conn.getHandshakePacket().setLoginIntention();

        //set the "proper" username here
        conn.getGameProfile().setUsername(packet.getUsername());

        conn.runInEventLoop(() -> this.handleVerdict0(conn, packet, info));
    }

    private void handleVerdict0(ClientConnection conn, PacketLoginStart packet, PreLoginInfo info) {
        switch (info.result()) {
            case DISCONNECTED -> conn.getFrameDecoder().releaseCollected();
            case CONNECT_TO_MAIN_SERVER -> {
                conn.uninjectWithRecoded(packet);
                /*if (recode)
                    conn.uninjectWithRecoded(packet);
                else
                    conn.uninject();*/
            }
            case CONNECT_TO_LIMBO -> {
                conn.setVerifyState(info.supplier());
                this.handleLogin1(conn);
            }
        }
    }

    private static final String tooManyPlayersMessage = "§eToo many players connected!";
    private static final PacketSnapshot
            CONFIG_TOO_MANY_PLAYERS = PacketConfigDisconnect.snapshot(tooManyPlayersMessage),
            LOGIN_TOO_MANY_PLAYERS = PacketLoginDisconnect.snapshot(tooManyPlayersMessage);

    private static final String unsupportedClientVersionMessage = "§eUnsupported client version!";
    private static final PacketSnapshot
            CONFIG_UNSUPPORTED_VERSION = PacketConfigDisconnect.snapshot(unsupportedClientVersionMessage),
            LOGIN_UNSUPPORTED_VERSION = PacketLoginDisconnect.snapshot(unsupportedClientVersionMessage);

    public void handle(ClientConnection conn, PacketLoginStart packet) {
        LimboJoinProfiler.update(conn.getChannel(), ConnectionStage.LOGIN_START);
        var addr = conn.getAddress();

        if (conn.sentLogin) {
            FireWallManager.add(addr, AlgorithmId.G1, true);
            conn.close();
            return;
        }

        RegisteredConnectionAlgoImpl.onLoginStartOrStatusRequest(conn.getChannel(), addr);

        conn.sentLogin = true;
        conn.getFrameDecoder().stopResendCollection();

        if (server.getConnections().getCount() >= server.getConfig().getMaxPlayers()) {
            var disconnectPacket = conn.isInConfigPhase() ? CONFIG_TOO_MANY_PLAYERS : LOGIN_TOO_MANY_PLAYERS;
            conn.sendPacketAndClose(disconnectPacket);
            return;
        }

        if (!conn.getClientVersion().isSupported()) {
            var disconnectPacket = conn.isInConfigPhase() ? CONFIG_UNSUPPORTED_VERSION : LOGIN_UNSUPPORTED_VERSION;
            conn.sendPacketAndClose(disconnectPacket);
            return;
        }

        this.handleLogin0(conn, packet);
    }

    public void handle(ClientConnection conn, PacketLoginAcknowledged packet) {
        conn.onLoginAcknowledgedReceived();
    }

    public void handle(ClientConnection conn, PacketInFinishConfiguration packet) {
        /*if (!conn.finishedConfig)
            throw NettySafety.INVALID_STATE;*/
        conn.spawnPlayer();
    }
}
