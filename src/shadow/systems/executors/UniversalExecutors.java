package shadow.systems.executors;

import shadow.systems.login.filters.ConnectionFilter;
import alix.common.scheduler.AlixScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.WorldSaveEvent;
import shadow.utils.command.managers.ChatManager;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.file.managers.UserFileManager;
import shadow.utils.users.User;
import shadow.utils.world.AlixWorldHolder;

import static shadow.utils.main.AlixUtils.*;
import static shadow.utils.users.UserManager.getVerifiedUser;

public abstract class UniversalExecutors implements Listener {

    //protected static final UUID mainWorldUUID = AlixWorldHolder.getMain().getUID();
    protected final ConnectionFilter[] premiumFilters = AlixHandler.getPremiumConnectionFilters();

    @EventHandler
    public void onSave(WorldSaveEvent e) {
        if (userDataAutoSave && AlixWorldHolder.isMain(e.getWorld())) AlixScheduler.async(UserFileManager::onAsyncSave);
        //WarpFileManager.save(); For now disabled
    }

    protected void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (interveneInChatFormat) e.setFormat(chatFormat);
        if (p.isOp()) {
            e.setMessage(translateColors(e.getMessage()));
            return;
        }
        User u = getVerifiedUser(p);
        if (ChatManager.cannotChat(u)) {
            e.setCancelled(true);
            return;
        }
        if (u.canSendColoredMessages()) e.setMessage(translateColors(e.getMessage()));
    }

    protected void onOperatorCommandCheck(PlayerCommandPreprocessEvent e, String cmd) {
        char[] t = cmd.toCharArray();
        if ((contains(t, "minecraft:") || contains(t, " run ")) && contains(t, "op ")) {
            e.getPlayer().sendMessage(colorize(isPluginLanguageEnglish ?
                    "&cPlease use /op or /deop instead." : "&cProszę użyć /op lub /deop zamiast tego."));
            e.setCancelled(true);
        }
    }
}