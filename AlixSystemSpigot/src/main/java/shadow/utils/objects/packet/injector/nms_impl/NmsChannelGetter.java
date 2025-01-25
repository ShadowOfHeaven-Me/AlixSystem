/*
package shadow.utils.objects.packet.injector.nms_impl;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

public interface NmsChannelGetter {

    Channel getChannel(Player player) throws Exception;

    String getVersion();

    static NmsChannelGetter createImpl() {
        try {
            //Unnecessary checks below
            //getFieldFromTypeDirect(entityPlayerClass, playerConnectionClass);
            //--getMethodByReturnTypeAssignable(playerConnectionClass, networkManagerClass);
            //getFieldFromTypeAssignable(playerConnectionClass, networkManagerClass);
            //getFieldFromTypeDirect(networkManagerClass, Channel.class);
            return new OldNmsChannelGetter();
        } catch (Throwable ignored) {
            return new NewerNmsChannelGetter();
        }
    }
}*/
