package ua.nanit.limbo.connection.captcha;

import alix.common.utils.other.throwable.AlixError;
import alix.libs.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.*;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.animation.PacketPlayInAnimation;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayInChunkBatchAck;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketInPlayKeepAlive;
import ua.nanit.limbo.protocol.packets.play.move.FlyingPacket;
import ua.nanit.limbo.protocol.packets.play.ping.PacketPlayInPong;
import ua.nanit.limbo.protocol.packets.play.slot.PacketPlayInHeldSlot;
import ua.nanit.limbo.protocol.packets.play.teleport.PacketPlayInTeleportConfirm;

import static ua.nanit.limbo.connection.captcha.CaptchaState.CaptchaPacketType.*;

public final class CaptchaState {

    private static final PacketSnapshot CAPTCHA_FAILED = PacketPlayOutDisconnect.error("Captcha Failed");
    private final CaptchaStateImpl impl;

    public CaptchaState(ClientConnection connection) {
        this.impl = new CaptchaStateImpl(connection);
    }

    enum CaptchaPacketType {
        BATCH_ACK,
        TP_CONFIRM,
        PONG,
        KEEP_ALIVE,
        ANIMATION,
        HELD_SLOT,
        FLYING
    }

    private <T extends PacketWrapper<T>> void handle0(T packet, CaptchaPacketType type) {
        try {
            this.invokeHandle0(packet, type);
        } catch (CaptchaFailedException ignored) {
            //ignored.printStackTrace();
            this.impl.disconnect(CAPTCHA_FAILED);
        }
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
            default:
                throw new AlixError();
        }
    }

    public void sendInitialKeepAlive() {
        this.impl.sendInitialKeepAlive();
    }

    public void handle(PacketPlayInChunkBatchAck packet) {
        this.handle0(packet.wrapper(), BATCH_ACK);
    }

    public void handle(PacketPlayInTeleportConfirm packet) {
        this.handle0(packet.wrapper(), TP_CONFIRM);
    }

    public void handle(PacketPlayInPong packet) {
        this.handle0(packet.wrapper(), PONG);
    }

    public void handle(PacketInPlayKeepAlive packet) {
        this.handle0(packet.wrapper(), KEEP_ALIVE);
    }

    public void handle(PacketPlayInAnimation packet) {
        this.handle0(packet.wrapper(), ANIMATION);
    }

    public void handle(PacketPlayInHeldSlot packet) {
        this.handle0(packet.wrapper(), HELD_SLOT);
    }

    public void handle(FlyingPacket packet) {
        this.handle0(packet.wrapper(), FLYING);
    }
}