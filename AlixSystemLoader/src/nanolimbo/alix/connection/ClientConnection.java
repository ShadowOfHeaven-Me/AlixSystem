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

import alix.common.utils.other.throwable.AlixException;
import io.netty.channel.Channel;
import nanolimbo.alix.connection.captcha.Captcha3d;
import nanolimbo.alix.connection.captcha.CaptchaState;
import nanolimbo.alix.connection.captcha.PacketPings;
import nanolimbo.alix.connection.pipeline.PacketDuplexHandler;
import nanolimbo.alix.connection.pipeline.VarIntFrameDecoder;
import nanolimbo.alix.protocol.PacketOut;
import nanolimbo.alix.protocol.PacketSnapshot;
import nanolimbo.alix.protocol.PacketSnapshots;
import nanolimbo.alix.protocol.packets.login.PacketConfigDisconnect;
import nanolimbo.alix.protocol.packets.play.keepalive.PacketPlayOutKeepAlive;
import nanolimbo.alix.protocol.registry.State;
import nanolimbo.alix.protocol.registry.Version;
import nanolimbo.alix.server.LimboServer;

import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public abstract class ClientConnection {

    private final LimboServer server;
    private final Channel channel;
    private final GameProfile gameProfile;

    private final PacketDuplexHandler duplexHandler;
    private final VarIntFrameDecoder frameDecoder;

    private final CaptchaState captchaState;

    private State state;
    private Version clientVersion;
    private SocketAddress address;

    public ClientConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder) {
        this.server = server;
        this.channel = channel;
        this.duplexHandler = duplexHandler;
        this.frameDecoder = frameDecoder;
        this.address = channel.unsafe().remoteAddress();
        this.gameProfile = new GameProfile();
        this.captchaState = new CaptchaState(this);
    }

    //removes the limbo handlers, sends the original packets to the server and allows a normal join
    public void uninject() {
        this.server.getClientChannelInitializer().uninject(this);
    }

    public void resendCollected() {
        this.frameDecoder.resendCollected(this.channel);
    }

    public Channel getChannel() {
        return channel;
    }

    public VarIntFrameDecoder getFrameDecoder() {
        return frameDecoder;
    }

    public PacketDuplexHandler getDuplexHandler() {
        return duplexHandler;
    }

    public UUID getUuid() {
        return gameProfile.getUuid();
    }

    public String getUsername() {
        return gameProfile.getUsername();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public Version getClientVersion() {
        return clientVersion;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public CaptchaState getCaptchaState() {
        return captchaState;
    }

    /*    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (state.equals(State.PLAY) || state.equals(State.CONFIGURATION)) {
            server.getConnections().removeConnection(this);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (channel.isActive()) {
            Log.error("Unhandled exception: ", cause);
        }
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        handlePacket(msg);
    }*/

/*    public void handlePacket(Object packet) {
        if (packet instanceof Packet) {
            ((Packet) packet).handle(this, server);
        }
    }*/

    public void fireLoginSuccess() {
        /*if (server.getConfig().getInfoForwarding().isModern() && velocityLoginMessageId == -1) {
            disconnectLogin("You need to connect with Velocity");
            return;
        }*/

        writeAndFlushPacket(PacketSnapshots.PACKET_LOGIN_SUCCESS);

        server.getConnections().addConnection(this);

        // Preparing for configuration mode
        if (clientVersion.moreOrEqual(Version.V1_20_2)) {
            updateEncoderState(State.CONFIGURATION);
            return;
        }

        spawnPlayer();
    }

    public void spawnPlayer() {
        updateState(State.PLAY);

        Runnable sendPlayPackets = () -> {
            writePacket(PacketSnapshots.PACKET_JOIN_GAME);
            writePacket(PacketSnapshots.PLAYER_ABILITIES_FALL);

            if (clientVersion.less(Version.V1_9)) {
                //the first packet here is unnecessary, but we do it to keep consistent across versions in bot checks
                writePacket(PacketSnapshots.PACKET_PLAYER_POS_AND_LOOK_LEGACY);
                writePacket(PacketSnapshots.PACKET_PLAYER_POS_AND_LOOK_LEGACY_VALID);
            } else {
                //first packet is necessary here to
                writePacket(PacketSnapshots.PACKET_PLAYER_POS_AND_LOOK);
                writePacket(PacketSnapshots.PACKET_PLAYER_POS_AND_LOOK_VALID);
            }

            if (clientVersion.moreOrEqual(Version.V1_19_3))
                writePacket(PacketSnapshots.PACKET_SPAWN_POSITION);

            if (server.getConfig().isUsePlayerList() || clientVersion.equals(Version.V1_16_4))
                writePacket(PacketSnapshots.PACKET_PLAYER_INFO);

            /*if (clientVersion.moreOrEqual(Version.V1_13)) {
                LimboCommand command = (LimboCommand) this.server.getCommandHandler().getCommandToSend().apply(this);
                this.writePacket(command.getPacketSnapshot());

                *//*if (PacketSnapshots.PACKET_PLAY_PLUGIN_MESSAGE != null)
                    writePacket(PacketSnapshots.PACKET_PLAY_PLUGIN_MESSAGE);*//*
            }*/

            if (PacketSnapshots.PACKET_BOSS_BAR != null && clientVersion.moreOrEqual(Version.V1_9))
                writePacket(PacketSnapshots.PACKET_BOSS_BAR);

            if (PacketSnapshots.PACKET_JOIN_MESSAGE != null)
                writePacket(PacketSnapshots.PACKET_JOIN_MESSAGE);

            if (PacketSnapshots.PACKET_TITLE_TITLE != null && clientVersion.moreOrEqual(Version.V1_8))
                writeTitle();

            if (PacketSnapshots.PACKET_HEADER_AND_FOOTER != null && clientVersion.moreOrEqual(Version.V1_8))
                writePacket(PacketSnapshots.PACKET_HEADER_AND_FOOTER);

            if (clientVersion.moreOrEqual(Version.V1_20_3)) {
                writePacket(PacketSnapshots.PACKET_START_WAITING_CHUNKS);

                for (PacketSnapshot chunk : PacketSnapshots.PACKETS_EMPTY_CHUNKS) {
                    writePacket(chunk);
                }
            }

            this.writePacket(PacketPings.INITIAL_PING);
            this.captchaState.sendInitialKeepAlive();

            Captcha3d.spinningDonut(this);
            //sendKeepAlive();
        };

        if (clientVersion.lessOrEqual(Version.V1_7_6)) {
            this.channel.eventLoop().schedule(sendPlayPackets, 100, TimeUnit.MILLISECONDS);
        } else {
            sendPlayPackets.run();
        }
    }

    public void onLoginAcknowledgedReceived() {
        updateState(State.CONFIGURATION);

        if (PacketSnapshots.PACKET_CONFIG_PLUGIN_MESSAGE != null)
            writePacket(PacketSnapshots.PACKET_CONFIG_PLUGIN_MESSAGE);

        if (clientVersion.moreOrEqual(Version.V1_20_5)) {
            for (PacketSnapshot packet : PacketSnapshots.PACKETS_REGISTRY_DATA) {
                writePacket(packet);
            }
        } else {
            writePacket(PacketSnapshots.PACKET_REGISTRY_DATA);
        }

        writeAndFlushPacket(PacketSnapshots.PACKET_FINISH_CONFIGURATION);
    }

    public void disconnectLogin(String reason) {
        if(!isConnected()) return;
        if (state == State.CONFIGURATION) {
            PacketConfigDisconnect disconnect = new PacketConfigDisconnect();
            disconnect.setReason(reason);
            sendPacketAndClose(disconnect);
        }
    }

    public void writeTitle() {
        if (clientVersion.moreOrEqual(Version.V1_17)) {
            writePacket(PacketSnapshots.PACKET_TITLE_TITLE);
            writePacket(PacketSnapshots.PACKET_TITLE_SUBTITLE);
            writePacket(PacketSnapshots.PACKET_TITLE_TIMES);
        } else {
            writePacket(PacketSnapshots.PACKET_TITLE_LEGACY_TITLE);
            writePacket(PacketSnapshots.PACKET_TITLE_LEGACY_SUBTITLE);
            writePacket(PacketSnapshots.PACKET_TITLE_LEGACY_TIMES);
        }
    }

    public void sendKeepAlive() {
        if (state.equals(State.PLAY)) {
            PacketPlayOutKeepAlive keepAlive = new PacketPlayOutKeepAlive();
            keepAlive.setId(ThreadLocalRandom.current().nextLong());
            writeAndFlushPacket(keepAlive);
        }
    }

    private void assertEventLoop() {
        if (!this.channel.eventLoop().inEventLoop()) throw new AlixException("Not in eventLoop");
    }

    private void safeExecute(Runnable task) {
        if (this.channel.eventLoop().inEventLoop()) task.run();
        else this.channel.eventLoop().execute(task);
    }

    public void writeAndFlushPacket(PacketOut packet) {
        this.duplexHandler.writeAndFlushPacket(packet);
        /*if (isConnected())
            this.safeExecute(() -> this.duplexHandler.writeAndFlushPacket(packet));*/
    }

    public void sendPacketAndClose(PacketOut packet) {
        //Log.error(String.joi (Thread.currentThread().getStackTrace()));
        new Exception().printStackTrace();
        this.duplexHandler.writeAndFlushPacket(packet, this.channel.newPromise()).addListener(UnsafeCloseFuture.INSTANCE);
        //this.channel.close();
        /*if (isConnected())
            this.safeExecute(() -> this.duplexHandler.writeAndFlushPacket(packet, this.channel.newPromise()).addListener(ChannelFutureListener.CLOSE));*/
    }

    public void flush() {
        this.duplexHandler.flush();
    }

    public void writePacket(PacketOut packet) {
        this.duplexHandler.writePacket(packet);
        /*if (isConnected())
            this.safeExecute(() -> this.duplexHandler.writePacket(packet));*/
    }

    public boolean isConnected() {
        return channel.isActive();
    }

    public void updateState(State state) {
        this.state = state;
        this.duplexHandler.updateState(state);
    }

    public void updateEncoderState(State state) {
        this.duplexHandler.updateEncoderState(state);
    }

    public void updateVersion(Version version) {
        this.clientVersion = version;
        this.duplexHandler.updateVersion(version);
    }

    /*public void setAddress(String host) {
        this.address = new InetSocketAddress(host, ((InetSocketAddress) this.address).getPort());
    }*/

/*    boolean checkBungeeGuardHandshake(String handshake) {
        String[] split = handshake.split("\00");

        if (split.length != 4)
            return false;

        String socketAddressHostname = split[1];
        UUID uuid = UuidUtil.fromString(split[2]);
        JsonArray arr;

        try {
            arr = JsonParser.array().from(split[3]);
        } catch (JsonParserException e) {
            return false;
        }

        String token = null;

        for (Object obj : arr) {
            if (obj instanceof JsonObject) {
                JsonObject prop = (JsonObject) obj;
                if (prop.getString("name").equals("bungeeguard-token")) {
                    token = prop.getString("value");
                    break;
                }
            }
        }

        if (!server.getConfig().getInfoForwarding().hasToken(token))
            return false;

        setAddress(socketAddressHostname);
        gameProfile.setUuid(uuid);

        Log.debug("Successfully verified BungeeGuard token");

        return true;
    }*/

    /*int getVelocityLoginMessageId() {
        return velocityLoginMessageId;
    }

    void setVelocityLoginMessageId(int velocityLoginMessageId) {
        this.velocityLoginMessageId = velocityLoginMessageId;
    }

    boolean checkVelocityKeyIntegrity(ByteMessage buf) {
        byte[] signature = new byte[32];
        buf.readBytes(signature);
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(server.getConfig().getInfoForwarding().getSecretKey(), "HmacSHA256"));
            byte[] mySignature = mac.doFinal(data);
            if (!MessageDigest.isEqual(signature, mySignature))
                return false;
        } catch (InvalidKeyException | java.security.NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        int version = buf.readVarInt();
        if (version != 1)
            throw new IllegalStateException("Unsupported forwarding version " + version + ", wanted " + '\001');
        return true;
    }*/
}
