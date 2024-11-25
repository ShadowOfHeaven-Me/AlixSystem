package shadow.virtualization;

import io.netty.channel.Channel;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.connection.pipeline.PacketDuplexHandler;
import nanolimbo.alix.connection.pipeline.VarIntFrameDecoder;
import nanolimbo.alix.server.LimboServer;

public final class LimboConnection extends ClientConnection {

    private VerificationReason verificationReason;

    public LimboConnection(Channel channel, LimboServer server, PacketDuplexHandler duplexHandler, VarIntFrameDecoder frameDecoder) {
        super(channel, server, duplexHandler, frameDecoder);
        this.verificationReason = VerificationReason.CAPTCHA;
    }

    public VerificationReason getVerificationReason() {
        return verificationReason;
    }

    public void setVerificationReason(VerificationReason verificationReason) {
        this.verificationReason = verificationReason;
    }
}