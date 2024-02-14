package shadow.systems.commands.alix;

import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import shadow.utils.users.User;
import shadow.utils.users.UserManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class ABStats {

    private static final Boolean PRESENT = Boolean.TRUE;
    private static final Map<UUID, Boolean> map = new ConcurrentHashMap<>();
    private static SchedulerTask task;

    //Returns: added now
    public static boolean reversePresence(User user) {
        return manageTask(map.compute(user.getUUID(), (i, p) -> p == null ? PRESENT : null)) != null;
    }

    private static void sendInfo0(User user, BaseComponent component) {
        user.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    private static Boolean manageTask(Boolean v) {
        switch (map.size()) {
            case 0:
                AntiBotStatistics.INSTANCE.markViewed(false);
                task.cancel();
                task = null;
                break;
            case 1:
                if (task == null)
                    AntiBotStatistics.INSTANCE.markViewed(true);
                    task = AlixScheduler.repeatAsync(() -> {
                        BaseComponent component = new TextComponent(AntiBotStatistics.INSTANCE.getFormattedStatistics());
                        for (UUID uuid : map.keySet()) {
                            User u = UserManager.getNullableUserOnline(uuid);
                            if (u == null) {
                                manageTask(map.remove(uuid));
                                continue;
                            }
                            sendInfo0(u, component);
                        }
                    }, 500, TimeUnit.MILLISECONDS);
                break;
        }
        return v;
    }
}