/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ua.nanit.limbo.connection;

import alix.common.scheduler.AlixScheduler;
import alix.common.utils.other.throwable.AlixError;
import ua.nanit.limbo.integration.PreLoginResult;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.configuration.PacketInFinishConfiguration;
import ua.nanit.limbo.protocol.packets.handshake.PacketHandshake;
import ua.nanit.limbo.protocol.packets.login.PacketConfigDisconnect;
import ua.nanit.limbo.protocol.packets.login.PacketLoginAcknowledged;
import ua.nanit.limbo.protocol.packets.login.PacketLoginStart;
import ua.nanit.limbo.protocol.packets.login.disconnect.PacketLoginDisconnect;
import ua.nanit.limbo.protocol.packets.status.PacketStatusRequest;
import ua.nanit.limbo.server.LimboServer;

public final class PacketHandler {

    private final LimboServer server;

    public PacketHandler(LimboServer server) {
        this.server = server;
    }

    public void handle(ClientConnection conn, PacketHandshake packet) {
        this.server.getIntegration().onHandshake(conn, packet);

        conn.updateVersion(packet.getVersion());
        conn.updateState(packet.getNextState());

        conn.setHandshakePacket(packet);
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

    public void handle(ClientConnection conn, PacketStatusRequest packet) {
        conn.getFrameDecoder().stopResendCollection();
        conn.uninject();
        //Log.error("STATUS REQUEST");
        //conn.sendPacket(new PacketStatusResponse(server));
    }

    /*public void handle(ClientConnection conn, PacketStatusPing packet) {
        conn.sendPacketAndClose(packet);
    }*/

    private void handleLogin1(ClientConnection conn) {
        //conn.ensureFirst();
        conn.getFrameDecoder().releaseCollected();
        conn.getDuplexHandler().tryEnableCompression();

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
        //Log.error("handleLogin0");
        boolean[] recodeA = new boolean[1];
        PreLoginResult result = this.server.getIntegration().onLoginStart(conn, packet, recodeA);
        //Log.error("RESULT-" + result);
        boolean recode = recodeA[0];

        //set the "proper" username here
        conn.getGameProfile().setUsername(packet.getUsername());
        //AlixCommonMain.logError("result: " + result + " recode " + recode);

        //Log.error("handleLogin0 - RESULT " + result);
        conn.getChannel().eventLoop().execute(() -> {
            switch (result) {
                case DISCONNECTED:
                    conn.getFrameDecoder().releaseCollected();
                    return;
                case CONNECT_TO_MAIN_SERVER:
                    if (recode) conn.uninjectWithRecoded(packet);
                    else conn.uninject();
                    return;
                case CONNECT_TO_LIMBO:
                    this.handleLogin1(conn);
                    return;
                default:
                    throw new AlixError();
            }
        });
    }

    private static final String tooManyPlayersMessage = "§7Too many players connected";
    private static final PacketSnapshot
            CONFIG_TOO_MANY_PLAYERS = PacketConfigDisconnect.snapshot(tooManyPlayersMessage),
            LOGIN_TOO_MANY_PLAYERS = PacketLoginDisconnect.snapshot(tooManyPlayersMessage);

    private static final String unsupportedClientVersionMessage = "§eUnsupported client version";
    private static final PacketSnapshot
            CONFIG_UNSUPPORTED_VERSION = PacketConfigDisconnect.snapshot(unsupportedClientVersionMessage),
            LOGIN_UNSUPPORTED_VERSION = PacketLoginDisconnect.snapshot(unsupportedClientVersionMessage);

    public void handle(ClientConnection conn, PacketLoginStart packet) {
        //Log.error("LOGIN START");
        conn.getFrameDecoder().stopResendCollection();

        if (server.getConnections().getCount() >= server.getConfig().getMaxPlayers()) {
            PacketSnapshot disconnectPacket = conn.isInConfigPhase() ? CONFIG_TOO_MANY_PLAYERS : LOGIN_TOO_MANY_PLAYERS;
            conn.sendPacketAndClose(disconnectPacket);
            //conn.disconnectLogin("Too many players connected");
            return;
        }

        if (!conn.getClientVersion().isSupported()) {
            PacketSnapshot disconnectPacket = conn.isInConfigPhase() ? CONFIG_UNSUPPORTED_VERSION : LOGIN_UNSUPPORTED_VERSION;
            conn.sendPacketAndClose(disconnectPacket);
            //conn.disconnectLogin("Unsupported client version");
            return;
        }

        AlixScheduler.asyncBlocking(() -> this.handleLogin0(conn, packet));
        //conn.getChannel().eventLoop().execute(() -> this.handleLogin0(conn, packet));
    }

    /*//WHY WON'T THIS WORK WHEN SCHEDULED, EVEN ON THE VERY SAME THREAD
    //I dunno, I give up, gonna have to block them netty threads
    @ScheduledForFix
    public void handleSex(ClientConnection conn, PacketLoginStart packet) {
        conn.getFrameDecoder().stopResendCollection();
        conn.getGameProfile().setUsername(packet.getUsername());

        if (server.getConnections().getCount() >= server.getConfig().getMaxPlayers()) {
            PacketSnapshot disconnectPacket = conn.isInConfigPhase() ? CONFIG_TOO_MANY_PLAYERS : LOGIN_TOO_MANY_PLAYERS;
            conn.sendPacketAndClose(disconnectPacket);
            //conn.disconnectLogin("Too many players connected");
            return;
        }

        if (!conn.getClientVersion().isSupported()) {
            PacketSnapshot disconnectPacket = conn.isInConfigPhase() ? CONFIG_UNSUPPORTED_VERSION : LOGIN_UNSUPPORTED_VERSION;
            conn.sendPacketAndClose(disconnectPacket);
            //conn.disconnectLogin("Unsupported client version");
            return;
        }
        PreLoginResult result = this.server.getIntegration().onLoginStart(conn, packet);
        //Log.error("handleLogin0 - RESULT " + result);
        switch (result) {
            case DISCONNECTED:
                conn.getFrameDecoder().releaseCollected();
                return;
            case CONNECT_TO_MAIN_SERVER:
                conn.getChannel().eventLoop().execute(conn::uninject);
                return;
            case CONNECT_TO_LIMBO:
                break;
        }

        conn.ensureFirst();
        conn.getFrameDecoder().releaseCollected();
        conn.getDuplexHandler().tryEnableCompression();

        conn.fireLoginSuccess();
    }*/

    /*public void handle(ClientConnection conn, PacketLoginPluginResponse packet) {
     *//*if (server.getConfig().getInfoForwarding().isModern()
                && packet.getMessageId() == conn.getVelocityLoginMessageId()) {

            if (!packet.isSuccessful() || packet.getData() == null) {
                conn.disconnectLogin("You need to connect with Velocity");
                return;
            }

            if (!conn.checkVelocityKeyIntegrity(packet.getData())) {
                conn.disconnectLogin("Can't verify forwarded player info");
                return;
            }

            // Order is important
            conn.setAddress(packet.getData().readString());
            conn.getGameProfile().setUuid(packet.getData().readUuid());
            conn.getGameProfile().setUsername(packet.getData().readString());

            conn.fireLoginSuccess();
        }*//*
    }*/

    public void handle(ClientConnection conn, PacketLoginAcknowledged packet) {
        conn.onLoginAcknowledgedReceived();
    }

    public void handle(ClientConnection conn, PacketInFinishConfiguration packet) {
        conn.spawnPlayer();
    }
}
