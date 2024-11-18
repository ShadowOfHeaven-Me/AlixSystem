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

package nanolimbo.alix.connection;

import nanolimbo.alix.protocol.packets.PacketHandshake;
import nanolimbo.alix.protocol.packets.configuration.PacketFinishConfiguration;
import nanolimbo.alix.protocol.packets.login.PacketLoginAcknowledged;
import nanolimbo.alix.protocol.packets.login.PacketLoginPluginResponse;
import nanolimbo.alix.protocol.packets.login.PacketLoginStart;
import nanolimbo.alix.protocol.packets.status.PacketStatusPing;
import nanolimbo.alix.protocol.packets.status.PacketStatusRequest;
import nanolimbo.alix.server.LimboServer;
import nanolimbo.alix.server.Log;

public final class PacketHandler {

    private final LimboServer server;

    public PacketHandler(LimboServer server) {
        this.server = server;
    }

    public void handle(ClientConnection conn, PacketHandshake packet) {
        this.server.getIntegration().onHandshake(conn, packet);
        conn.updateVersion(packet.getVersion());
        conn.updateState(packet.getNextState());

        Log.debug("Pinged from %s [%s]", conn.getAddress(), conn.getClientVersion().toString());

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

    public void handle(ClientConnection conn, PacketStatusPing packet) {
        conn.sendPacketAndClose(packet);
    }

    public void handle(ClientConnection conn, PacketLoginStart packet) {
        conn.getFrameDecoder().stopResendCollection();
        switch (this.server.getIntegration().onLoginStart(conn, packet)) {
            case DISCONNECTED:
                conn.getFrameDecoder().releaseCollected();
                return;
            case CONNECT_TO_MAIN_SERVER:
                conn.uninject();
                return;
            case CONNECT_TO_LIMBO:
                break;
        }

        /*if (this.server.getIntegration().hasCompletedCaptcha(packet.getUsername(), conn.getChannel())) {
            conn.uninject();
            return;
        }*/
        conn.getFrameDecoder().releaseCollected();
        conn.getDuplexHandler().tryEnableCompression();

        if (server.getConfig().getMaxPlayers() > 0 &&
                server.getConnections().getCount() >= server.getConfig().getMaxPlayers()) {
            conn.disconnectLogin("Too many players connected");
            return;
        }

        if (!conn.getClientVersion().isSupported()) {
            conn.disconnectLogin("Unsupported client version");
            return;
        }

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

    public void handle(ClientConnection conn, PacketLoginPluginResponse packet) {
        /*if (server.getConfig().getInfoForwarding().isModern()
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
        }*/
    }

    public void handle(ClientConnection conn, PacketLoginAcknowledged packet) {
        conn.onLoginAcknowledgedReceived();
    }

    public void handle(ClientConnection conn, PacketFinishConfiguration packet) {
        conn.spawnPlayer();
    }
}
