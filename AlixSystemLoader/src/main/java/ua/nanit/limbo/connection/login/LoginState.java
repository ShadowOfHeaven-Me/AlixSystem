package ua.nanit.limbo.connection.login;

import alix.common.data.PersistentUserData;
import alix.common.utils.other.throwable.AlixException;
import ua.nanit.limbo.commands.LimboCommand;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.connection.captcha.KeepAlives;
import ua.nanit.limbo.connection.login.gui.LimboGUI;
import ua.nanit.limbo.connection.login.gui.LimboPinBuilder;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.PacketSnapshots;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;
import ua.nanit.limbo.protocol.packets.play.PacketPlayerPositionAndLook;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInClickSlot;
import ua.nanit.limbo.protocol.packets.play.inventory.PacketPlayInInventoryClose;
import ua.nanit.limbo.protocol.packets.play.move.FlyingPacket;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;

import java.util.Arrays;
import java.util.function.Consumer;

public final class LoginState implements VerifyState {

    private static final LimboCommand LOGIN = LimboCommand.construct(Arrays.asList("login"), "password");
    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private LimboGUI gui;
    private Consumer<ClientConnection> authAction;

    public LoginState(ClientConnection connection) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
    }
    public void tryLogIn() {
        this.logIn();
    }

    public void logIn() {
        if (this.authAction == null) throw new AlixException("authAction is null! Report this immediately!");

        this.connection.removeLimboHandlers();
        this.authAction.accept(this.connection);
    }

    @Override
    public void setData(PersistentUserData data, Consumer<ClientConnection> authAction) {
        this.authAction = authAction;
        this.gui = new LimboPinBuilder(this.connection, data, this);

        /*this.gui = new LimboAuthBuilder(this.connection, MapSecretKey.fromName(this.connection.getUsername()), correct -> {
            if (correct) {
                this.logIn();
                return;
            }
        }, true);*/
    }

    //private static final float INITIAL_YAW = 8.59e+8f;

    @Override
    public void sendInitial() {
        this.write(PacketSnapshots.PLAYER_ABILITIES_FLY);
        this.write(LOGIN.getPacketSnapshot());
        //this.write(new PacketPlayerPositionAndLook(0.5, 64, 0.5, INITIAL_YAW, 0, 2));
        this.write(new PacketPlayerPositionAndLook(0.5, 64, 0.5, 0, 0, 2));

        if (this.gui != null) this.gui.show();
        else this.duplexHandler.flush();

        Log.error("LOGIN SENT");
    }

    @Override
    public void handle(PacketPlayInClickSlot packet) {
        if (this.gui != null) this.gui.select(packet.wrapper().getSlot());
    }

    @Override
    public void handle(PacketPlayInInventoryClose packet) {
        if (this.gui != null) this.gui.onCloseAttempt();
    }

    private long lastKeepAliveSentTime;
    private float lastYaw = 0;

    @Override
    public void handle(FlyingPacket packet) {
        long now = System.currentTimeMillis();
        long lastKeepAliveSent = now - lastKeepAliveSentTime;

        if (lastKeepAliveSent >= 15000) {
            this.writeAndFlush(KeepAlives.KEEP_ALIVE_PREVENT_TIMEOUT);
            this.lastKeepAliveSentTime = now;
        }

        var wrapper = packet.wrapper();

        if (wrapper.hasRotationChanged()) {
            var yaw = wrapper.getLocation().getYaw();
            float deltaYaw = Math.abs(yaw - this.lastYaw);

            var msg = "Yaw: " + yaw + " deltaYaw: " + deltaYaw;
            Log.error(msg);
            this.writeAndFlush(PacketPlayOutMessage.withMessage("§c" + msg));

            this.lastYaw = yaw;
        }
    }

    void disconnect(PacketOut disconnectPacket) {
        this.connection.sendPacketAndClose(disconnectPacket);
    }

    private void write(PacketOut packet) {
        this.duplexHandler.write(packet);
    }

    private void writeAndFlush(PacketOut packet) {
        this.duplexHandler.writeAndFlush(packet);
    }

    private Version version() {
        return this.connection.getClientVersion();
    }
}