package shadow.utils.objects.packet.injector.nms_impl;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import shadow.utils.holders.ReflectionUtils;

import java.lang.reflect.Field;

final class NewerNmsChannelGetter implements NmsChannelGetter {

    //Source code: https://github.com/KaspianDev/AntiPopup/blob/master/v1.20.2/src/main/java/com/github/kaspiandev/antipopup/nms/v1_20_2/PlayerInjector_v1_20_2.java

    private static final Field //getPacketListener,
            getConnection, getNetworkManager, getChannel;

    static {
        /*Field packetListenerField = null;
        Class<?> packetListenerClazz = ServerCommonPacketListenerImpl.class;
        //EntityPlayer
        for (Field field : ReflectionUtils.getHandle.getReturnType().getDeclaredFields()) {//EntityPlayer class
            if (field.getType() == packetListenerClazz) {
                field.setAccessible(true);
                packetListenerField = field;
                break;
            }
        }

        assert packetListenerField != null;
        getPacketListener = packetListenerField;
        Main.logError("dfgtyhjuikol " + getPacketListener + " " + ReflectionUtils.getHandle.getReturnType());
        AlixUtils.debug(ReflectionUtils.getHandle.getReturnType().getDeclaredFields());*/


        Class<?> connectionClazz = ReflectionUtils.playerConnectionClass;
        Field connectionField = null;
        for (Field field : ReflectionUtils.getHandle.getReturnType().getDeclaredFields()) {
            if (field.getType() == connectionClazz) {
                field.setAccessible(true);
                connectionField = field;
                break;
            }
        }
        if (connectionField == null) throw new AssertionError();
        getConnection = connectionField;

        Class<?> managerClazz = ReflectionUtils.networkManagerClass;
        Field managerField = null;
        try {
            for (Field field : Class.forName("net.minecraft.server.network.ServerCommonPacketListenerImpl").getDeclaredFields()) {
                if (managerClazz.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    managerField = field;
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (managerField == null) throw new AssertionError();
        getNetworkManager = managerField;

        Field channelField = null;
        for (Field field : managerClazz.getDeclaredFields()) {
            if (Channel.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                channelField = field;
                break;
            }
        }

        //AlixUtils.debug(connectionClazz.getDeclaredFields());

        if (channelField == null) throw new AssertionError();
        getChannel = channelField;
    }

    @Override
    public Channel getChannel(Player p) throws Exception {
        Object entityPlayer = ReflectionUtils.getHandle.invoke(p);
        Object connection = getConnection.get(entityPlayer);
        Object networkManager = getNetworkManager.get(connection);
        return (Channel) getChannel.get(networkManager);
    }

    @Override
    public String getVersion() {
        return "1.20.2+";
    }
}