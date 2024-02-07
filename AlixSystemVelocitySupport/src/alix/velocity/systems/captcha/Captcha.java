package alix.velocity.systems.captcha;

import io.netty.channel.Channel;
import net.elytrium.limboapi.api.player.LimboPlayer;

public interface Captcha {

    void sendPackets(Channel channel);

}