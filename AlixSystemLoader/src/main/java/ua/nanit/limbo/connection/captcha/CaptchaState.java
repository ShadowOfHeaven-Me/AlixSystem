package ua.nanit.limbo.connection.captcha;

import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.animation.PacketPlayInAnimation;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayInChunkBatchAck;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayInCookieResponse;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.packets.play.held.PacketPlayInHeldSlot;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketInPlayKeepAlive;
import ua.nanit.limbo.protocol.packets.play.move.FlyingPacket;
import ua.nanit.limbo.protocol.packets.play.ping.PacketPlayInPong;
import ua.nanit.limbo.protocol.packets.play.rename.PacketPlayInItemRename;
import ua.nanit.limbo.protocol.packets.play.teleport.PacketPlayInTeleportConfirm;
import ua.nanit.limbo.protocol.packets.play.tick.PacketPlayInTickEnd;
import ua.nanit.limbo.protocol.packets.play.transaction.PacketPlayInTransaction;
import ua.nanit.limbo.server.Log;

import static ua.nanit.limbo.connection.captcha.CaptchaState.CaptchaPacketType.*;

public final class CaptchaState implements VerifyState {

    private static final PacketSnapshot CAPTCHA_FAILED = PacketPlayOutDisconnect.error("Captcha Failed");
    private final CaptchaStateImpl impl;

    public CaptchaState(ClientConnection connection) {
        this.impl = new CaptchaStateImpl(connection);
    }

    enum CaptchaPacketType {
        BATCH_ACK,
        TP_CONFIRM,
        PONG,
        WINDOW_CONFIRM,
        KEEP_ALIVE,
        ANIMATION,
        HELD_SLOT,
        FLYING,
        COOKIE_RESPONSE,
        TICK_END
    }

    private <T extends PacketWrapper<T>> void handle0(T packet, CaptchaPacketType type) {
        try {
            this.invokeHandle0(packet, type);
        } catch (CaptchaFailedException ex) {
            if (NanoLimbo.performChecks) this.disconnect();
            else Log.error("CaptchaFailedException, performChecks=false");

            if (NanoLimbo.printCaptchaFailed) ex.printStackTrace();
        }
    }

    private void disconnect() {
        this.impl.disconnect(CAPTCHA_FAILED);
    }

    private <T extends PacketWrapper<T>> void invokeHandle0(T packet, CaptchaPacketType type) {
        switch (type) {
            case BATCH_ACK:
                this.impl.handle((WrapperPlayClientChunkBatchAck) packet);
                return;
            case TP_CONFIRM:
                this.impl.handle((WrapperPlayClientTeleportConfirm) packet);
                return;
            case PONG:
                this.impl.handle((WrapperPlayClientPong) packet);
                return;
            case WINDOW_CONFIRM:
                this.impl.handle((WrapperPlayClientWindowConfirmation) packet);
                return;
            case KEEP_ALIVE:
                this.impl.handle((WrapperPlayClientKeepAlive) packet);
                return;
            case ANIMATION:
                this.impl.handle((WrapperPlayClientAnimation) packet);
                return;
            case HELD_SLOT:
                this.impl.handle((WrapperPlayClientHeldItemChange) packet);
                return;
            case FLYING:
                this.impl.handle((WrapperPlayClientPlayerFlying) packet);
                return;
            case COOKIE_RESPONSE:
                this.impl.handle((WrapperPlayClientCookieResponse) packet);
                return;
            case TICK_END:
                this.impl.handle((WrapperPlayClientClientTickEnd) packet);
                return;
            default:
                throw new AlixError();
        }
    }

    @Override
    public void sendInitial() {
        this.impl.sendInitial0();
    }

    @Override
    public void handle(PacketPlayInItemRename packet) {
        this.disconnect();
    }

    @Override
    public void handle(PacketPlayInTickEnd packet) {
        this.handle0(PacketPlayInTickEnd.WRAPPER, TICK_END);
    }

    @Override
    public void handle(PacketPlayInChunkBatchAck packet) {
        this.handle0(packet.wrapper(), BATCH_ACK);
    }

    @Override
    public void handle(PacketPlayInTeleportConfirm packet) {
        this.handle0(packet.wrapper(), TP_CONFIRM);
    }

    @Override
    public void handle(PacketPlayInPong packet) {
        this.handle0(packet.wrapper(), PONG);
    }

    @Override
    public void handle(PacketPlayInTransaction packet) {
        this.handle0(packet.wrapper(), WINDOW_CONFIRM);
    }

    @Override
    public void handle(PacketInPlayKeepAlive packet) {
        this.handle0(packet.wrapper(), KEEP_ALIVE);
    }

    @Override
    public void handle(PacketPlayInAnimation packet) {
        this.handle0(packet.wrapper(), ANIMATION);
    }

    @Override
    public void handle(PacketPlayInHeldSlot packet) {
        this.handle0(packet.wrapper(), HELD_SLOT);
    }

    @Override
    public void handle(FlyingPacket packet) {
        this.handle0(packet.wrapper(), FLYING);
    }

    @Override
    public void handle(PacketPlayInCookieResponse packet) {
        this.handle0(packet.wrapper(), COOKIE_RESPONSE);
    }
}