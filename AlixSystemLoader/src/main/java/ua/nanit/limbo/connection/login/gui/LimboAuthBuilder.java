package ua.nanit.limbo.connection.login.gui;

import alix.common.data.PersistentUserData;
import alix.common.utils.other.keys.secret.MapSecretKey;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.login.gui.bedrock.AbstractAuthBuilder;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;

import java.util.UUID;
import java.util.function.Consumer;

public final class LimboAuthBuilder extends AbstractAuthBuilder implements LimboGUI {
    @Override
    protected void write(PacketOut packet) {
        this.duplexHandler.write(packet);
    }

    @Override
    protected void writeAndFlush(PacketOut packet) {
        this.duplexHandler.writeAndFlush(packet);
    }

    @Override
    protected void sendPacketAndClose(PacketSnapshot packet) {
        this.connection.sendPacketAndClose(packet);
    }

    @Override
    protected ClientVersion getClientVersion() {
        return this.connection.getRetrooperClientVersion();
    }

    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;

    public LimboAuthBuilder(ClientConnection connection, PersistentUserData data, Consumer<Boolean> onConfirm, boolean includeLeaveButton) {
        super(data, onConfirm, includeLeaveButton);
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        if (connection.getVerifyState() instanceof LoginState loginState && loginState.data != null && loginState.data.canUseAnyRecovery()) {
            this.setOnRecover(b -> loginState.openRecovery());
        }
    }
}