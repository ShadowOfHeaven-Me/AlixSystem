package shadow.systems.login.captcha.manager;

import io.netty.channel.Channel;
import shadow.Main;
import shadow.utils.holders.packet.buffered.BufferedPackets;

public final class CountdownTask {//show count down and kick out

    private final Channel channel;
    private Object[] packets;
    private int index;

    public CountdownTask(Channel channel, boolean loginCountdown) {
        this.channel = channel;
        this.index = loginCountdown ? BufferedPackets.loginPacketArraySize : BufferedPackets.captchaPacketArraySize;
        this.packets = loginCountdown ? BufferedPackets.loginOutExperiencePackets : BufferedPackets.captchaOutExperiencePackets;
    }

    //Returns: Whether the player should be kicked
    public boolean tick() {
        if (index == 0) return true;
        this.channel.writeAndFlush(this.packets[--this.index]);
        return false;
    }

    public void setToLogin() {
        this.packets = BufferedPackets.loginOutExperiencePackets;
        this.index = BufferedPackets.loginPacketArraySize;
    }

    public static void pregenerate() {
        BufferedPackets.init();
    }
}