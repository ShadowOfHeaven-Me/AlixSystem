package ua.nanit.limbo.connection;

import alix.common.data.PersistentUserData;
import alix.common.utils.floodgate.GeyserUtil;
import alix.common.utils.other.throwable.AlixError;
import ua.nanit.limbo.protocol.packets.play.animation.PacketPlayInAnimation;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayInChunkBatchAck;
import ua.nanit.limbo.protocol.packets.play.config.PacketPlayInReconfigureAck;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayInCookieResponse;
import ua.nanit.limbo.protocol.packets.play.held.PacketPlayInHeldSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInClickSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInInventoryClose;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketInPlayKeepAlive;
import ua.nanit.limbo.protocol.packets.play.move.FlyingPacket;
import ua.nanit.limbo.protocol.packets.play.ping.PacketPlayInPong;
import ua.nanit.limbo.protocol.packets.play.rename.PacketPlayInItemRename;
import ua.nanit.limbo.protocol.packets.play.teleport.PacketPlayInTeleportConfirm;
import ua.nanit.limbo.protocol.packets.play.tick.PacketPlayInTickEnd;
import ua.nanit.limbo.protocol.packets.play.transaction.PacketPlayInTransaction;

import java.util.function.Consumer;

final class DummyVerifyState implements VerifyState {

    static final DummyVerifyState INSTANCE = new DummyVerifyState();
    private static final AlixError ERROR = new AlixError("DummyVerifyState touched");

    @Override
    public void sendInitial() {
        throw ERROR;
    }

    @Override
    public boolean isLoginState() {
        throw ERROR;
    }

    @Override
    public void setData(PersistentUserData data, Consumer<ClientConnection> authAction, GeyserUtil geyserUti) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInReconfigureAck packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInItemRename packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInClickSlot packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInInventoryClose packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInChunkBatchAck packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInTeleportConfirm packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInPong packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInTransaction packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketInPlayKeepAlive packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInAnimation packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInHeldSlot packet) {
        throw ERROR;
    }

    @Override
    public void handle(FlyingPacket packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInCookieResponse packet) {
        throw ERROR;
    }

    @Override
    public void handle(PacketPlayInTickEnd packet) {
        throw ERROR;
    }

    @Override
    public void onLimboDisconnect() {
        throw ERROR;
    }
}