package ua.nanit.limbo.connection;

import alix.common.data.PersistentUserData;
import alix.common.utils.floodgate.GeyserUtil;
import ua.nanit.limbo.connection.login.LoginState;
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

public interface VerifyState {

    void sendInitial();

    default boolean isLoginState() {
        return this instanceof LoginState;
    }

    default void setData(PersistentUserData data, Consumer<ClientConnection> authAction, GeyserUtil geyserUti) {
    }

    default void handle(PacketPlayInReconfigureAck packet) {
    }

    default void handle(PacketPlayInItemRename packet) {
    }

    default void handle(PacketPlayInClickSlot packet) {
    }

    default void handle(PacketPlayInInventoryClose packet) {
    }

    default void handle(PacketPlayInChunkBatchAck packet) {
    }

    default void handle(PacketPlayInTeleportConfirm packet) {
    }

    default void handle(PacketPlayInPong packet) {
    }

    default void handle(PacketPlayInTransaction packet) {
    }

    default void handle(PacketInPlayKeepAlive packet) {
    }

    default void handle(PacketPlayInAnimation packet) {
    }

    default void handle(PacketPlayInHeldSlot packet) {
    }

    default void handle(FlyingPacket packet) {
    }

    default void handle(PacketPlayInCookieResponse packet) {
    }

    default void handle(PacketPlayInTickEnd packet) {
    }

    default void onLimboDisconnect() {
    }
}