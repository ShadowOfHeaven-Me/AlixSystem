package ua.nanit.limbo.connection.login.countdown;

import alix.common.messages.Messages;
import io.netty.util.concurrent.ScheduledFuture;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.captcha.KeepAlives;
import ua.nanit.limbo.connection.login.packets.ExperiencePackets;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.snapshot.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.registry.State;

import java.util.concurrent.TimeUnit;

public final class LimboCountdown {//shows xp countdown and kicks out

    private static final PacketSnapshot
            registerTimePassedKickPacket = PacketPlayOutDisconnect.snapshot(Messages.get("register-time-passed")),
            loginTimePassedKickPacket = PacketPlayOutDisconnect.snapshot(Messages.get("login-time-passed")),
            emailVerificationTimePassedKickPacket = PacketPlayOutDisconnect.snapshot(Messages.get("register-email-verification-time-passed"));

    //private final ChannelHandlerContext ctx;
    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private final PacketSnapshot[] packets;
    private final PacketSnapshot kickPacket;
    private final ScheduledFuture<?> task;
    private int index;

    public LimboCountdown(ClientConnection connection, boolean isRegistered) {
        this(connection, ExperiencePackets.PACKETS, ExperiencePackets.PACKET_COUNT,
                isRegistered ? loginTimePassedKickPacket : registerTimePassedKickPacket);
    }

    //A separate entry point used only while a 'require-email-in-register' registration is waiting on the
    //verification email to arrive (see LoginState#handleRegisterCommandWithEmail()) - uses the
    //independently-configurable 'email-verification-time' duration/packet set (ConfigParams#emailVerificationTime)
    //instead of the general login/register one, since receiving an email can easily take longer than
    //max-login-time allows for the rest of the login/register GUI.
    public LimboCountdown(ClientConnection connection) {
        this(connection, ExperiencePackets.EMAIL_VERIFICATION_PACKETS, ExperiencePackets.EMAIL_VERIFICATION_PACKET_COUNT,
                emailVerificationTimePassedKickPacket);
    }

    private LimboCountdown(ClientConnection connection, PacketSnapshot[] packets, int count, PacketSnapshot kickPacket) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        this.index = count;
        this.packets = packets;
        this.kickPacket = kickPacket;
        this.task = this.connection.getChannel().eventLoop().scheduleWithFixedDelay(this::tick, 500, ExperiencePackets.UPDATE_PERIOD_MILLI, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        this.task.cancel(false);
    }

    private long lastKeepAliveSentTime;

    private void keepAlive() {
        long now = System.currentTimeMillis();
        long lastKeepAliveSent = now - lastKeepAliveSentTime;

        if (lastKeepAliveSent >= 10_000) {
            this.lastKeepAliveSentTime = now;
            this.duplexHandler.writeAndFlush(KeepAlives.KEEP_ALIVE_PREVENT_TIMEOUT);
        }
    }

    void tick() {
        if (this.connection.getEncoderState() != State.PLAY) {
            if (--this.index == 0) this.connection.closeTimedOut();
            return;
        }
        if (this.index != 0) {
            //if (!this.connection.isInPlayPhase()) return;
            this.keepAlive();
            this.duplexHandler.writeAndFlush(this.packets[--this.index]);
            return;
        }
        this.connection.sendPacketAndClose(this.kickPacket);
    }
}