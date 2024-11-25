package shadow.utils.misc.methods;

import alix.common.utils.other.annotation.AlixIntrinsified;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import shadow.utils.misc.packet.constructors.OutDisconnectPacketConstructor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.users.types.AlixUser;

import java.util.concurrent.CompletableFuture;

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

    private static final ByteBuf CLOSE_INVENTORY_BUFFER = NettyUtils.constBuffer(new WrapperPlayServerCloseWindow(0));

    //Silently - does not fire InventoryCloseEvent
    @AlixIntrinsified(method = "Player#closeInventory")
    public static void closeInventoryAsyncSilently(ChannelHandlerContext silentContext) {
        NettyUtils.writeAndFlushConst(silentContext, CLOSE_INVENTORY_BUFFER);
    }

    @AlixIntrinsified(method = "Player#kickPlayer")
    public static void kickAsyncLoginDynamic(Channel channel, String kickMessage) {
        NettyUtils.closeAfterDynamicSend(channel, OutDisconnectPacketConstructor.constructDynamicAtLoginPhase(kickMessage));
    }

    @AlixIntrinsified(method = "Player#kickPlayer")
    public static void kickAsync(AlixUser user, ByteBuf disconnectPacket) {
        NettyUtils.closeAfterConstSend(user.silentContext(), disconnectPacket);
    }

    public static final PlayerTeleportEvent.TeleportCause ASYNC_TP_CAUSE = PlayerTeleportEvent.TeleportCause.SPECTATE;

    @AlixIntrinsified(method = "Player#teleport")
    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location loc) {
        return PaperLib.teleportAsync(entity, loc, ASYNC_TP_CAUSE);
    }

    @AlixIntrinsified(method = "Player#teleport")
    public static CompletableFuture<Boolean> teleportAsyncPluginCause(Entity entity, Location loc) {
        return PaperLib.teleportAsync(entity, loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public static void init() {
    }
}