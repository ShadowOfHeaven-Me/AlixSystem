package shadow.utils.objects.savable.data.gui;

import alix.common.data.LoginType;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import shadow.utils.holders.packet.constructors.OutDisconnectKickPacketConstructor;
import shadow.utils.objects.savable.data.gui.virtual.VirtualInventory;

public interface AlixVerificationGui {

    /**
     * Throws an exception if the used gui cannot be returned
     *
     * @return The Inventory gui used for password building
     * @author ShadowOfHeaven
     */

    @NotNull
    VirtualInventory getVirtualGUI();

    /**
     * Opens the GUI and informs this gui about it
     *
     * @author ShadowOfHeaven
     */

    default void openGUI() {
        this.getVirtualGUI().open();
    }


    /**
     * Informs the gui about a slot select
     *
     * @param slot The inventory slot clicked
     * @author ShadowOfHeaven
     */

    void select(int slot);

    /**
     * Returns the gui type
     *
     * @return The gui type
     * @author ShadowOfHeaven
     */

    @NotNull
    LoginType getType();


    /**
     * @return The currently created password with the builder
     * @author ShadowOfHeaven
     */

    @NotNull
    String getPasswordBuilt();

    ByteBuf pinLeaveFeedbackKickPacket = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase(PasswordGui.pinLeaveFeedback);

/*    static void onSyncClick(UnverifiedUser user, InventoryClickEvent event) {
        AlixVerificationGui builder = user.getPasswordBuilder();
        int slot = event.getRawSlot();

        if (builder.getType() == LoginType.ANVIL) {
            switch (slot) {
                case 1:
                    MethodProvider.kickAsync(user, pinLeaveFeedbackKickPacket);
                    break;
                case 2:
                    String password = builder.getPasswordBuilt();
                    if (password.isEmpty()) return;
                    if (user.isRegistered()) {
                        if (user.isPasswordCorrect(password)) {
                            user.logIn();
                            return;
                        } else if (++user.loginAttempts == maxLoginAttempts)
                            MethodProvider.kickAsync(user, CommandManager.incorrectPasswordKickPacket);
                        return;
                    }
                    String reason = AlixUtils.getInvalidityReason(password, false);
                    if (reason != null) {
                        user.sendDynamicMessageSilently(reason);
                        user.getPlayer().playSound(user.getCurrentLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    user.writeAndFlushConstSilently(CommandManager.passwordRegisterMessagePacket);
                    user.registerSync(password);
                    return;
            }
            return;
        }
        builder.select(slot);
    }

    static void onAsyncClick(UnverifiedUser user, int slot) {//async as of - async to the main thread, but only having 1 invoking thread per gui
        AlixVerificationGui builder = user.getPasswordBuilder();

        if (builder.getType() == LoginType.ANVIL) {
            switch (slot) {
                case 1:
                    MethodProvider.kickAsync(user, pinLeaveFeedbackKickPacket);
                    break;
                case 2:
                    String password = builder.getPasswordBuilt();
                    if (password.isEmpty()) return;
                    if (user.isRegistered()) {
                        if (user.isPasswordCorrect(password)) {
                            user.logIn();
                            return;
                        } else if (++user.loginAttempts == maxLoginAttempts)
                            MethodProvider.kickAsync(user, CommandManager.incorrectPasswordKickPacket);
                        return;
                    }
                    String reason = AlixUtils.getInvalidityReason(password, false);
                    if (reason != null) {
                        user.sendDynamicMessageSilently(reason);
                        user.getPlayer().playSound(user.getCurrentLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        return;
                    }
                    user.writeAndFlushConstSilently(CommandManager.passwordRegisterMessagePacket);
                    user.registerAsync(password);
                    return;
            }
            return;
        }
        builder.select(slot);
    }*/
}