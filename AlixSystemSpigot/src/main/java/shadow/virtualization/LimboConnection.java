package shadow.virtualization;

import io.netty.channel.Channel;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.VerifyState;
import ua.nanit.limbo.server.LimboServer;

import java.util.function.Function;

public final class LimboConnection extends ClientConnection {

    public LimboConnection(Channel channel, LimboServer server, Function<ClientConnection, VerifyState> state) {
        super(channel, server, state);
    }

    //private VerificationReason verificationReason;



    /*public VerificationReason getVerificationReason() {
        return verificationReason;
    }

    public void setVerificationReason(VerificationReason verificationReason) {
        this.verificationReason = verificationReason;
    }*/
}