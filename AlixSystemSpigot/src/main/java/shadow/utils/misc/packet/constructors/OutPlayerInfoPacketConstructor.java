package shadow.utils.misc.packet.constructors;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shadow.utils.netty.NettyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public final class OutPlayerInfoPacketConstructor {

/*    private static final Object ADD_PLAYER;
    private static final Constructor<?> constructor;
    //private static final boolean enumSetConstructor;

    static {
        Class<?> packetClass = ReflectionUtils.outPlayerInfoPacketClass;
        Class<? extends Enum> playerInfoActionClazz = null;
        for (Class<?> clazz : packetClass.getClasses()) {
            if (clazz.isEnum()) {
                playerInfoActionClazz = (Class<? extends Enum>) clazz;
                break;
            }
        }
        //Main.logError(Arrays.toString(packetClass.getConstructors()) + " ");
        Object ADD_PLAYER0 = playerInfoActionClazz.getEnumConstants()[0];//it's an enum constant
        Constructor<?> constructor0;
        //finding the right constructor with a few try catches
        try {
            constructor0 = ReflectionUtils.getConstructor(packetClass, playerInfoActionClazz, Collection.class);
        } catch (Throwable e) {
            try {
                constructor0 = ReflectionUtils.getConstructor(packetClass, playerInfoActionClazz, Iterable.class);
            } catch (Throwable ex) {
                constructor0 = ReflectionUtils.getConstructor(packetClass, EnumSet.class, Collection.class);
                Enum UPDATE_LIST = playerInfoActionClazz.getEnumConstants()[3];
                Enum UPDATE_DISPLAY_NAME = playerInfoActionClazz.getEnumConstants()[5];
                //ClientboundPlayerInfoUpdatePacket
                ADD_PLAYER0 = EnumSet.of((Enum) ADD_PLAYER0, UPDATE_LIST, UPDATE_DISPLAY_NAME);//it's overriden to be an EnumSet
            }
        }
        constructor = constructor0;
        ADD_PLAYER = ADD_PLAYER0;
    }

    public static Object constructADD(Collection<Object> nmsPlayers) {
        try {
            return constructor.newInstance(ADD_PLAYER, nmsPlayers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

/*    private static Object construct_ADD_ENUM_SET(Collection<Object> nmsPlayers) {
        try {
            return constructor.newInstance(ADD_PLAYER, nmsPlayers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object construct_ADD_PRE_1_19_3(Collection<Object> nmsPlayers) {
        try {
            return constructor.newInstance(ADD_PLAYER, nmsPlayers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    private static final ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> UPDATE_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE, WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME);

    private static final WrapperPlayServerPlayerInfo.Action[] INFO_ACTIONS = new WrapperPlayServerPlayerInfo.Action[]{
            WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME, WrapperPlayServerPlayerInfo.Action.UPDATE_GAME_MODE
    };

    public static ByteBuf[] construct_ADD_OF_ALL_VISIBLE(Player sendTo, ChannelHandlerContext context) {
        if (version.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> list = new ArrayList<>(Bukkit.getOnlinePlayers().size());

            for (Player p : Bukkit.getOnlinePlayers()) {
                User u = PacketEvents.getAPI().getPlayerManager().getUser(p);

                if (u != null && isVisible(sendTo, p))
                    list.add(new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(u.getProfile(), true, 0, SpigotConversionUtil.fromBukkitGameMode(p.getGameMode()), Component.text(p.getPlayerListName()), null));
            }
            return new ByteBuf[]{NettyUtils.dynamic(new WrapperPlayServerPlayerInfoUpdate(UPDATE_ACTIONS, list), context)};
        }

        List<WrapperPlayServerPlayerInfo.PlayerData> list = new ArrayList<>(Bukkit.getOnlinePlayers().size());

        for (Player p : Bukkit.getOnlinePlayers()) {
            User u = PacketEvents.getAPI().getPlayerManager().getUser(p);

            if (u != null && isVisible(sendTo, p))
                list.add(new WrapperPlayServerPlayerInfo.PlayerData(Component.text(p.getPlayerListName()), u.getProfile(), SpigotConversionUtil.fromBukkitGameMode(p.getGameMode()), null, 0));
        }

        ByteBuf[] buffers = new ByteBuf[INFO_ACTIONS.length];

        for (int i = 0; i < buffers.length; i++) {
            WrapperPlayServerPlayerInfo.Action action = INFO_ACTIONS[i];
            buffers[i] = NettyUtils.dynamic(new WrapperPlayServerPlayerInfo(action, list), context);
        }
        return buffers;
    }

    public static ByteBuf[] constructConst_ADD_OF_ONE_VISIBLE(User addToTab, Player p) {
        if (version.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo info = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(addToTab.getProfile(), true, 0, SpigotConversionUtil.fromBukkitGameMode(p.getGameMode()), Component.text(p.getPlayerListName()), null);

            return new ByteBuf[]{NettyUtils.constBuffer(new WrapperPlayServerPlayerInfoUpdate(UPDATE_ACTIONS, Collections.singletonList(info)))};
        }

        WrapperPlayServerPlayerInfo.PlayerData info = new WrapperPlayServerPlayerInfo.PlayerData(Component.text(p.getPlayerListName()), addToTab.getProfile(), SpigotConversionUtil.fromBukkitGameMode(p.getGameMode()), null, 0);

        List<WrapperPlayServerPlayerInfo.PlayerData> list = Collections.singletonList(info);
        ByteBuf[] buffers = new ByteBuf[INFO_ACTIONS.length];

        for (int i = 0; i < buffers.length; i++) {
            WrapperPlayServerPlayerInfo.Action action = INFO_ACTIONS[i];
            buffers[i] = NettyUtils.constBuffer(new WrapperPlayServerPlayerInfo(action, list));
        }
        return buffers;
    }

    public static boolean isVisible(Player looker, Player target) {
        //UnverifiedUser user;
        return target == null  || looker == null //fix for fake players
                || looker.canSee(target);// && ((user = Verifications.get(target)) == null || !user.isCaptchaInitialized());
    }

    public static void init() {
    }

    private OutPlayerInfoPacketConstructor() {
    }
}