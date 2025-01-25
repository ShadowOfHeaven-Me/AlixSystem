package shadow.systems.commands.alix;

import alix.common.antibot.algorithms.connection.AntiBotStatistics;
import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import shadow.utils.users.types.VerifiedUser;
import shadow.utils.users.UserManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class ABStats {

    private static final Boolean PRESENT = Boolean.TRUE;
    private static final Map<UUID, Boolean> map = new ConcurrentHashMap<>();
    private static SchedulerTask task;

    //Returns: true if it was added now, false otherwise
    public static boolean reversePresence(VerifiedUser user) {
        return manageTask(map.compute(user.getUUID(), (i, p) -> p == null ? PRESENT : null)) != null;
    }

    private static void sendInfo0(VerifiedUser user, BaseComponent component) {
        user.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    private static Boolean manageTask(Boolean v) {
        switch (map.size()) {
            case 0:
                AntiBotStatistics.INSTANCE.markViewed(false);
                task.cancel();
                task = null;
                //AntiBotStatistics.INSTANCE.reset();
                break;
            case 1:
                if (task != null) return v;
                if (task == null)
                    AntiBotStatistics.INSTANCE.markViewed(true);
                task = AlixScheduler.repeatAsync(new Runnable() {

                    private boolean reset;

                    @Override
                    public void run() {
                        BaseComponent component = new TextComponent(AntiBotStatistics.INSTANCE.getFormattedStatistics());
                        for (UUID uuid : map.keySet()) {
                            VerifiedUser u = UserManager.getNullableVerifiedUser(uuid);
                            if (u == null) {
                                manageTask(map.remove(uuid));
                                continue;
                            }
                            sendInfo0(u, component);
                        }

                        this.reset = !this.reset;
                        if (reset) AntiBotStatistics.INSTANCE.reset();
                    }
                }, 500, TimeUnit.MILLISECONDS);
                break;
        }
        return v;
    }
}