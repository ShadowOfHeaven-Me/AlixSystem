package shadow.systems.executors.gui;

import alix.common.messages.Messages;
import alix.common.messages.types.AlixMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import shadow.systems.commands.CommandManager;
import shadow.systems.login.Verifications;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.password.builders.BuilderType;
import shadow.utils.objects.savable.data.password.builders.PasswordBuilder;
import shadow.utils.objects.savable.data.password.builders.PasswordGui;
import shadow.utils.users.offline.UnverifiedUser;

import static shadow.systems.commands.CommandManager.incorrectPassword;
import static shadow.systems.commands.CommandManager.loginSuccess;
import static shadow.utils.main.AlixUtils.kickOnIncorrectPassword;
import static shadow.utils.main.AlixUtils.sendMessage;

public final class GUIExecutors implements Listener {

    private final String pinRegister = Messages.get("pin-register");
    private final AlixMessage pinRegisterBottomLine = Messages.getAsObject("pin-register-bottom-line");

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        UnverifiedUser user = Verifications.get(event.getWhoClicked().getUniqueId());

        if (user == null) return;

        if (!user.isGuiUser() || !user.isGUIInitialized()) {//not a pin use or the gui has not been initialized yet
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        PasswordBuilder builder = user.getPasswordBuilder();

        if (builder.getType() == BuilderType.ANVIL) {
            switch (event.getRawSlot()) {
                case 1:
                    user.getPlayer().kickPlayer(PasswordGui.pinLeaveFeedback);
                    break;
                case 2:
                    String password = user.getPasswordBuilder().getPasswordBuilt();
                    if (password.isEmpty()) return;
                    if (user.isRegistered()) {
                        if (user.isPasswordCorrect(password)) {
                            sendMessage(user.getPlayer(), loginSuccess);
                            user.logIn();
                            return;
                        } else if (kickOnIncorrectPassword) user.getPlayer().kickPlayer(incorrectPassword);
                        return;
                    }
                    String reason = AlixUtils.getInvalidityReason(password, false);
                    if (reason != null) {
                        sendMessage(user.getPlayer(), reason);
                        user.getPlayer().playSound(user.getCurrentLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    sendMessage(user.getPlayer(), CommandManager.passwordRegister);
                    user.register(password);
                    return;
            }
            return;
        }

        if (builder.select(event.getRawSlot())) {
            String pin = builder.getPasswordBuilt();

            if (user.isRegistered()) {
                if (user.isPasswordCorrect(pin)) {
                    user.logIn();
                    sendMessage(user.getPlayer(), loginSuccess);
                    return;
                } else if (kickOnIncorrectPassword) user.getPlayer().kickPlayer(incorrectPassword);
                return;
            }
            user.register(pin);
            user.getPlayer().sendTitle(pinRegister, pinRegisterBottomLine.format(pin), 0, 100, 50);
        }
    }

/*    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        UnverifiedUser user = Verifications.get(event.getPlayer().getUniqueId());

        if (user == null) return;//user.isPinGUIInitialized();

        if (user.isGuiUser() && user.isGUIInitialized() && user.getPasswordBuilder().getType() == BuilderType.PIN) {
            AlixScheduler.runLaterSync(user::openPasswordBuilderGUI, 100, TimeUnit.MILLISECONDS);
        }
    }*/
}