package ua.nanit.limbo.connection;

import alix.common.data.PersistentUserData;
import ua.nanit.limbo.protocol.packets.play.animation.PacketPlayInAnimation;
import ua.nanit.limbo.protocol.packets.play.batch.PacketPlayInChunkBatchAck;
import ua.nanit.limbo.protocol.packets.play.cookie.PacketPlayInCookieResponse;
import ua.nanit.limbo.protocol.packets.play.held.PacketPlayInHeldSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInClickSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInInventoryClose;
import ua.nanit.limbo.protocol.packets.play.keepalive.PacketInPlayKeepAlive;
import ua.nanit.limbo.protocol.packets.play.move.FlyingPacket;
import ua.nanit.limbo.protocol.packets.play.ping.PacketPlayInPong;
import ua.nanit.limbo.protocol.packets.play.teleport.PacketPlayInTeleportConfirm;
import ua.nanit.limbo.protocol.packets.play.transaction.PacketPlayInTransaction;

import java.util.function.Consumer;

public interface VerifyState {

    void sendInitial();

    default void setData(PersistentUserData data, Consumer<ClientConnection> authAction) {
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
}