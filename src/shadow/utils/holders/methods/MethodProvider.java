package shadow.utils.holders.methods;

import alix.common.utils.netty.NettyUtils;
import alix.common.utils.other.annotation.AlixIntrinsified;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import shadow.utils.users.offline.UnverifiedUser;

public final class MethodProvider {

    //private static final MethodProvider instance = new MethodProvider();
    //private static final UserKickMethod kickMethod = UserKickMethod.createImpl();

    /*private static final PaperLibAccess paperLib;

    static {
        //we use the current class loader of AlixSystem.jarinjar, so we cannot do: "AlixSystem.jarinjar" + File.separator + "PaperLibJar.jarinjar"
        //This would only be correct if the class loader was from AlixLoader.jar
        JarInJarClassLoader loader = new JarInJarClassLoader(MethodProvider.class.getClassLoader(), "PaperLibJar.jarinjar");
        PaperLibAccess paperLib0;
        try {
            paperLib0 = (PaperLibAccess) loader.instantiateClass("alix.paperlib.PaperLibAccessImpl", PaperLibAccess.class);
        } catch (Exception e) {
            throw new AlixException(e);
        } finally {
            loader.close();
        }
        paperLib = paperLib0;
    }*/

    static {
        PaperLib.isPaper();//load the class
    }

    public static void kickAsync(UnverifiedUser user, Object disconnectPacket) {
        kickAsync(user.getPacketBlocker().getChannel(), disconnectPacket);
    }

    @AlixIntrinsified(method = "Player#kickPlayer")
    public static void kickAsync(Channel channel, Object disconnectPacket) {
        NettyUtils.writeAndFlush(channel, disconnectPacket, ChannelFutureListener.CLOSE);
    }

    @AlixIntrinsified(method = "Player#teleport")
    public static void teleportAsync(Player player, Location loc) {
        PaperLib.teleportAsync(player, loc);
    }

    public static void init() {
    }
}