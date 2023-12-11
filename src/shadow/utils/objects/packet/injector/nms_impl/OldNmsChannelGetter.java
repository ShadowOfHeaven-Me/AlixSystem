package shadow.utils.objects.packet.injector.nms_impl;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Field;

import static shadow.utils.holders.ReflectionUtils.*;

final class OldNmsChannelGetter implements NmsChannelGetter {

    private static final Field getPlayerConnection = getFieldFromTypeDirect(entityPlayerClass, playerConnectionClass);
    private static final Field getNetworkManager = getFieldFromTypeAssignable(playerConnectionClass, networkManagerClass);
    private static final Field getChannel = getFieldFromTypeDirect(networkManagerClass, Channel.class);

    @Override
    public Channel getChannel(Player p) throws Exception {
        Object entityPlayer = ReflectionUtils.getHandle.invoke(p);
        Object playerConnection = getPlayerConnection.get(entityPlayer);
        Object networkManager = getNetworkManager.get(playerConnection);
        return (Channel) getChannel.get(networkManager);
    }

    @Override
    public String getVersion() {
        return "PRE";
    }
}