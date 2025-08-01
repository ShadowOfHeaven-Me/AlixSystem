package alix.velocity.server.impl.user;

import io.netty.channel.Channel;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.server.LimboServer;

import java.util.function.Function;

public final class VelocityClientConnection extends ClientConnection {

    public VelocityClientConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state) {
        super(channel, server, state);
    }
}