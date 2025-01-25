package shadow.virtualization;

import io.netty.channel.Channel;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.connection.pipeline.VarIntFrameDecoder;
import ua.nanit.limbo.server.LimboServer;

public final class LimboConnection extends ClientConnection {

    //private VerificationReason verificationReason;

    public LimboConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder) {
        super(channel, server, duplexHandler, frameDecoder);
        //this.verificationReason = VerificationReason.CAPTCHA;
    }

    /*public VerificationReason getVerificationReason() {
        return verificationReason;
    }

    public void setVerificationReason(VerificationReason verificationReason) {
        this.verificationReason = verificationReason;
    }*/
}