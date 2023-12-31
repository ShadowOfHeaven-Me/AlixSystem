package shadow.utils.command.tpa;

import alix.common.messages.Messages;
import alix.common.utils.formatter.AlixFormatter;
import org.bukkit.entity.Player;
import shadow.Main;
import alix.common.scheduler.AlixScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;

import java.util.concurrent.TimeUnit;

public final class TpaRequest { //extends AlixIdentifiable {

    public static final long tpaAutoExpire = Main.config.getLong("tpa-auto-expire");
    private static final boolean runAutoExpireTimer = tpaAutoExpire > 0;
    private static final String teleportRequestExpired = Messages.get("tpa-request-expired");
    private final Player from, to;
    private final SchedulerTask autoExpireTask;
    private final String fromName;

    protected TpaRequest(Player from, Player to, String fromName) {
        this.from = from; //teleporting
        this.to = to; //accepting - the destination
        this.fromName = fromName;
        this.autoExpireTask = runAutoExpireTimer ? AlixScheduler.runLaterSync(() -> {
            TpaManager.removeRequest(fromName);
            if (from.isOnline())
                from.sendMessage(AlixUtils.colorize(AlixFormatter.formatSingle(teleportRequestExpired, to.getName())));
        }, tpaAutoExpire, TimeUnit.SECONDS) : null;
    }

    public void remove() {
        if (autoExpireTask != null) autoExpireTask.cancel();
        TpaManager.removeRequest(fromName);
    }

    public void accept() {
        AlixHandler.delayedConfigTeleport(from, to.getLocation());
        this.remove();
    }

    public String getSentToName() {
        return to.getName();
    }

/*    public boolean isSentFrom(Player p) {
        return p.getUniqueId().equals(from.getUniqueId());
    }

    public boolean isSentTo(Player p) {
        return p.getUniqueId().equals(to.getUniqueId());
    }*/

}