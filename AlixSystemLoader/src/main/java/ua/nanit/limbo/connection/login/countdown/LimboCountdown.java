package ua.nanit.limbo.connection.login.countdown;

import alix.common.messages.Messages;
import io.netty.util.concurrent.ScheduledFuture;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.packets.ExperiencePackets;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.registry.State;

import java.util.concurrent.TimeUnit;

public final class LimboCountdown {//shows xp countdown and kicks out

    private static final PacketSnapshot
            registerTimePassedKickPacket = PacketPlayOutDisconnect.snapshot(Messages.get("register-time-passed")),
            loginTimePassedKickPacket = PacketPlayOutDisconnect.snapshot(Messages.get("login-time-passed"));

    //private final ChannelHandlerContext ctx;
    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private final PacketSnapshot[] packets;
    private final boolean isRegistered;
    private final ScheduledFuture<?> task;
    private int index;

    public LimboCountdown(ClientConnection connection, boolean isRegistered) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        this.index = ExperiencePackets.PACKET_COUNT;
        this.packets = ExperiencePackets.PACKETS;
        this.isRegistered = isRegistered;
        this.task = this.connection.getChannel().eventLoop().scheduleWithFixedDelay(this::tick, 500, ExperiencePackets.UPDATE_PERIOD_MILLI, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        this.task.cancel(false);
    }

    void tick() {
        if (this.connection.getEncoderState() != State.PLAY) {
            if(--this.index == 0) {
                this.connection.closeTimedOut();
            }
            return;
        }
        if (this.index != 0) {
            //if (!this.connection.isInPlayPhase()) return;
            this.duplexHandler.writeAndFlush(this.packets[--this.index]);
            return;
        }
        this.connection.sendPacketAndClose(this.isRegistered ? loginTimePassedKickPacket : registerTimePassedKickPacket);
    }
}