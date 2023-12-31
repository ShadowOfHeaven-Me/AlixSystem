package shadow.utils.objects.savable.data.gui;

import alix.common.data.GuiType;
import alix.common.scheduler.AlixScheduler;
import org.bukkit.Sound;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import shadow.systems.commands.CommandManager;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.offline.UnverifiedUser;

import static shadow.systems.commands.CommandManager.loginSuccess;
import static shadow.utils.main.AlixUtils.kickOnIncorrectPassword;
import static shadow.utils.main.AlixUtils.sendMessage;

public interface AlixGui {

    /**
     * Throws an exception if the used gui cannot be returned
     *
     * @return The Inventory gui used for password building
     * @author ShadowOfHeaven
     */

    @NotNull
    default Inventory getGUI() {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns whether the player should be registered/logged in after the slot select
     *
     * @param slot The inventory slot clicked
     * @author ShadowOfHeaven
     */

    default void select(int slot) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the gui type
     *
     * @return The gui type
     * @author ShadowOfHeaven
     */

    GuiType getType();


    /**
     * @return The currently created password with the builder
     * @author ShadowOfHeaven
     */

    @NotNull
    String getPasswordBuilt();

    Object pinLeaveFeedbackKickPacket = OutDisconnectKickPacketConstructor.constructAtPlayPhase(PasswordGui.pinLeaveFeedback);

    static void onSyncClick(UnverifiedUser user, int slot) {
        AlixGui builder = user.getPasswordBuilder();

        if (builder.getType() == GuiType.ANVIL) {
            switch (slot) {
                case 1:
                    MethodProvider.kickAsync(user, pinLeaveFeedbackKickPacket);
                    //user.getPlayer().kickPlayer(PasswordGui.pinLeaveFeedback);
                    break;
                case 2:
                    String password = builder.getPasswordBuilt();
                    if (password.isEmpty()) return;
                    if (user.isRegistered()) {
                        if (user.isPasswordCorrect(password)) {
                            sendMessage(user.getPlayer(), loginSuccess);
                            user.logInSync();
                            return;
                        } else if (kickOnIncorrectPassword)
                            MethodProvider.kickAsync(user, CommandManager.incorrectPasswordKickPacket);
                        return;
                    }
                    String reason = AlixUtils.getInvalidityReason(password, false);
                    if (reason != null) {
                        sendMessage(user.getPlayer(), reason);
                        user.getPlayer().playSound(user.getCurrentLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    sendMessage(user.getPlayer(), CommandManager.passwordRegister);
                    user.registerSync(password);
                    return;
            }
            return;
        }
        builder.select(slot);
    }

    static void onAsyncClick(UnverifiedUser user, int slot) {//async as of - async to the main thread, but only having 1 invoking thread per gui
        AlixGui builder = user.getPasswordBuilder();

        if (builder.getType() == GuiType.ANVIL) {
            switch (slot) {
                case 1:
                    MethodProvider.kickAsync(user, pinLeaveFeedbackKickPacket);
                    //AlixScheduler.sync(() -> user.getPlayer().kickPlayer(PasswordGui.pinLeaveFeedback));
                    break;
                case 2:
                    String password = builder.getPasswordBuilt();
                    if (password.isEmpty()) return;
                    if (user.isRegistered()) {
                        if (user.isPasswordCorrect(password)) {
                            sendMessage(user.getPlayer(), loginSuccess);
                            user.logInAsync();
                            return;
                        } else if (kickOnIncorrectPassword)
                            MethodProvider.kickAsync(user, CommandManager.incorrectPasswordKickPacket);
                        return;
                    }
                    String reason = AlixUtils.getInvalidityReason(password, false);
                    if (reason != null) {
                        sendMessage(user.getPlayer(), reason);
                        user.getPlayer().playSound(user.getCurrentLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    sendMessage(user.getPlayer(), CommandManager.passwordRegister);
                    user.registerAsync(password);
                    return;
            }
            return;
        }
        builder.select(slot);
    }
}