package shadow.utils.objects.packet.types.verified;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import shadow.systems.commands.CommandManager;
import shadow.systems.commands.alix.AlixCommandManager;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.users.User;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.Arrays;

public final class AlixDuplexHandler extends ChannelDuplexHandler {

    private static final String packetHandlerName = "alixsystem_ver_handler";
    private final User user;
    private final Channel channel;
    //private final Map<String, Object> map;

    private AlixDuplexHandler(User user, Channel channel) {
        this.user = user;
        this.channel = channel;
        //this.map = new ConcurrentHashMap<>(16);
        AlixHandler.inject(channel, packetHandlerName, this);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*if (msg.getClass() == ReflectionUtils.outPlayerInfoPacketClass) {
            //this.decode(msg);
            return;
        }*/
        if (PacketBlocker.serverboundNameVersion && msg.getClass() == PacketBlocker.commandPacketClass) {
            String cmd = (String) PacketBlocker.getStringFromCommandPacketMethod.invoke(msg);
            String[] splet = AlixUtils.split(cmd, ' ');
            if (AlixCommandManager.isPasswordChangeCommand(splet[0])) {
                CommandManager.onPasswordChangeCommand(user, Arrays.copyOfRange(splet, 1, splet.length));
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

/*    private void decode(Object packet) {
        try {
            List<?> playerActionList = (List<?>) ReflectionUtils.getPlayerInfoDataListMethod.invoke(packet);
            Object playerAction = playerActionList.get(0);
            GameProfile profile = (GameProfile) ReflectionUtils.getProfileFromPlayerInfoDataMethod.invoke(playerAction);
            String name = profile.getName();
            if (!LoginVerdictManager.getNullable(name).getVerdict().isAutoLogin())
                this.map.put(name, packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

/*    public void sendOf(String name) {
        Object packet = this.map.get(name);
        if (packet != null)//excluding the self packet send
            this.channel.writeAndFlush(packet);
    }*/

    public void stop() {
        this.channel.eventLoop().submit(() -> {
            this.channel.pipeline().remove(packetHandlerName);
            return null;
        });
    }

    public static AlixDuplexHandler getHandler(User user, Channel channel) {
        return channel != null ? new AlixDuplexHandler(user, channel) : null;
    }
}