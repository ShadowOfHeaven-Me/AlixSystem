/*
package shadow.utils.objects.filter.packet.ping;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import alix.common.messages.Messages;
import alix.common.scheduler.impl.JavaScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import shadow.utils.holders.ReflectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PingCheck implements Runnable {

    //id = time sent
    private static final String pingCheckFailedMessage = Messages.get("ping-check-failed");
    //private static final int pingThreshold = 250; //Main.config.getInt("ping-threshold");
    //private static final byte maxStrikeCount = 5;// (byte) Math.min(Math.max((byte) Main.config.getInt("max-strike-count"), 1), 10);
    private final Set<Long> set;
    private final Channel channel;
    private final Player player;
    private final SchedulerTask task;
    private int pingsReceived;
    private int pingTotal;
    private boolean cancelled;

    public PingCheck(Player player, Channel channel) {
        this.set = Collections.synchronizedSet(ConcurrentHashMap.newKeySet());
        this.player = player;
        this.channel = channel;
        this.task = JavaScheduler.repeatAsync(this, 100, 50, TimeUnit.MILLISECONDS);
        JavaScheduler.runLaterAsync(this::endResult, 5000, TimeUnit.MILLISECONDS);
    }

    public boolean hasFinished() {
        return cancelled;
    }

    public void endResult() {
        //if (!set.isEmpty()) JavaScheduler.sync(() -> player.kickPlayer("Ping too high!"));
        cancel();
        //JavaUtils.broadcastFast0( "Name " + player.getName() + " Ping Total: " + pingTotal + " Strike: " + pingsReceived + " Ping avg: " + ((float) pingTotal / pingsReceived) + " Set size: " + set.size());
    }

    public synchronized void cancel() {
        if (cancelled) return;

        cancelled = true;
        task.cancel();
    }

    public boolean handle(Object packet) {
        try {
            long time = (long) ReflectionUtils.getKeepAliveMethod.invoke(packet);

            if (set.remove(time)) {//existed, it must be from us
                int ping = (int) (System.currentTimeMillis() - time);

                //Bukkit.broadcastMessage("Ping: " + ping);

                this.pingTotal += ping;
                pingsReceived++;

                //JavaUtils.broadcastFast0("Ping: " + ping);

                */
/*if (ping > pingThreshold) {
                    if(++strike == maxStrikeCount) {
                        player.kickPlayer(pingCheckFailedMessage);
                    }
                }*//*


                //Bukkit.broadcastMessage("Ping: " + ping);
                return false;//hide it from the server
            }
            //Bukkit.broadcastMessage("ZEEET");
            return true;//it was from the server
            //return !set.remove(ReflectionUtils.getKeepAliveMethod.invoke(packet));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        Object keepAlivePacket;
        try {
            keepAlivePacket = ReflectionUtils.outKeepAliveConstructor.newInstance(time);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        set.add(time);
        channel.writeAndFlush(keepAlivePacket);
        //Bukkit.broadcastMessage("Spoofed");
    }
}*/
