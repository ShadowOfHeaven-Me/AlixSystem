/*
package shadow.utils.objects.filter.packet.types;

import io.netty.channel.ChannelHandlerContext;
import org.bukkit.entity.Player;
import shadow.utils.objects.filter.packet.ping.PingCheck;
import shadow.utils.users.offline.UnverifiedUser;

public class PingCheckPacketBlocker extends DefaultPacketBlocker {

    private final PingCheck pingCheck;

    public PingCheckPacketBlocker(Player p, UnverifiedUser u) {
        super(p, u);
        this.pingCheck = new PingCheck(p, channel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {//Check for null because for some fucked up reason this method can be invoked even before the class itself is initialized
        if (pingCheck != null && pingCheck.hasFinished()) {
            super.channelRead(ctx, msg);
            return;
        }
        //if (checkForKick()) return;
        if (msg.getClass().getSimpleName().equals("PacketPlayInKeepAlive")) {
            if (pingCheck == null) super.channelRead(ctx, msg);
            if (this.pingCheck.handle(msg)) {
                super.channelRead(ctx, msg);
            }
        }
    }

    @Override
    public void stop() {
        this.pingCheck.cancel();
        super.stop();
    }
}*/
