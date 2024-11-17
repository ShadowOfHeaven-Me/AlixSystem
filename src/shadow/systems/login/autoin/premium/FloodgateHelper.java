package shadow.systems.login.autoin.premium;

//https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/paper/FloodgateHelper.java

import alix.libs.com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import shadow.systems.dependencies.floodgate.FloodgateAccess;
import shadow.utils.misc.methods.MethodProvider;

final class FloodgateHelper {

    private static final AttributeKey<String> kickMessageAttribute = AttributeKey.valueOf("floodgate-kick-message");

    //returns true if the login is allowed, false if a kick occurred
    static boolean processFloodgateTasks(Channel channel, WrapperLoginClientLoginStart packet) {
        FloodgatePlayer floodgatePlayer = FloodgateAccess.getBedrockPlayer(channel);
        if (floodgatePlayer == null) {
            return true;
        }

        // kick the player, if necessary
        String kickMessage = channel.hasAttr(kickMessageAttribute) ? channel.attr(kickMessageAttribute).get() : "<No kick message>";
        if (kickMessage != null) {
            MethodProvider.kickAsyncLoginDynamic(channel, kickMessage);
            return false;
        }

        // add prefix
        String username = floodgatePlayer.getCorrectUsername();
        packet.setUsername(username);

        // remove real Floodgate data handler
        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.context("floodgate_data_handler") != null) pipeline.remove("floodgate_data_handler");

        /*ChannelHandler floodgateHandler = channel.pipeline().get("floodgate_data_handler");
        channel.pipeline().remove(floodgateHandler);*/
        return true;
    }
}