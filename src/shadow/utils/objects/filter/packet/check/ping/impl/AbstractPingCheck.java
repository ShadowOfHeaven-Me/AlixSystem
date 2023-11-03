package shadow.utils.objects.filter.packet.check.ping.impl;

import alix.common.messages.Messages;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.Main;
import alix.common.scheduler.impl.AlixScheduler;
import shadow.utils.objects.filter.packet.check.ping.PingCheck;

import java.util.concurrent.TimeUnit;

public abstract class AbstractPingCheck implements PingCheck {

    private static final int pingChecksRequired = Main.config.getInt("ping-checks");
    private static final int maxPingSum = Main.config.getInt("ping-threshold") * pingChecksRequired * 2;// *2 as the packet had to travel both ways - so we divide it by 2 times that
    private static final String pingCheckFailedMessage = Messages.get("ping-check-failed");

    private final Channel channel;
    private final Player player;
    private long pingSum;
    private int pingChecksToSend, pingsReceived;
    private boolean sendingCompleted = false;

    protected AbstractPingCheck(Channel channel, Player player) {
        this.channel = channel;
        this.player = player;
        this.pingChecksToSend = pingChecksRequired;
        AlixScheduler.runLaterAsync(this::sendKeepAlive, 100, TimeUnit.MILLISECONDS);
    }

    protected abstract long getId(Object packet);

    protected abstract Object createPacket(long id);

    //Returns: whether the server can process the packet - It's not our own
    @Override
    public boolean receiveKeepAlive(Object keepAlivePacket) {
        if (sendingCompleted) return true;//all of our packets have been already received
        long id = this.getId(keepAlivePacket);

        //Checks whether the last 17 binary places are zeroes - 0b11111111111111111 is the same as 131072
        boolean isOurId = (id & 131072) == 0; //Same as ((id >> 17) << 17) == id

        Main.logInfo("received - " + isOurId);

        if (isOurId) {

            long time = id >> 17;

            long ping = System.currentTimeMillis() - time;//(now - sentFromUsTime)

            this.pingSum += ping;

            if (++this.pingsReceived == pingChecksRequired) {
                this.sendingCompleted = true;
                this.concludePingCheck();
            } else AlixScheduler.runLaterAsync(this::sendKeepAlive, 50, TimeUnit.MILLISECONDS); //JavaScheduler.async(this::sendKeepAlive);

            return false;//it was our own packet - disallow the server from processing it
        }
        return true;//the packet was from the server - let the packet be processed by the server
    }

    @Override
    public boolean isCompleted() {
        return sendingCompleted;
    }

    private void concludePingCheck() {
        Main.logInfo("ping sum: " + pingSum + " max " + maxPingSum + " ping: " + (pingSum / (2L * pingChecksRequired)));
        if (pingSum > maxPingSum) {//the player has failed the ping check
            AlixScheduler.sync(() -> player.kickPlayer(pingCheckFailedMessage));
        }
    }

    private void sendKeepAlive() {
        if (this.pingChecksToSend-- == 0) return;

        long id = System.currentTimeMillis() << 17;//2^17 = 131072

        Object keepAlivePacket = this.createPacket(id);

        this.channel.writeAndFlush(keepAlivePacket);

        Main.logInfo("sent");
    }
}