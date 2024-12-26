package shadow.utils.objects.savable.data.gui.builders;

import alix.common.data.LoginType;
import org.jetbrains.annotations.NotNull;
import shadow.systems.commands.CommandManager;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.methods.MethodProvider;
import shadow.utils.misc.packet.buffered.AnvilPacketSuppliers;
import shadow.utils.misc.packet.constructors.AlixInventoryType;
import shadow.utils.objects.savable.data.gui.AlixJavaVerificationGui;
import shadow.utils.objects.savable.data.gui.PasswordGui;
import shadow.utils.objects.savable.data.gui.bases.AnvilBuilderBase;
import shadow.utils.objects.savable.data.gui.virtual.EmptyVirtualInventory;
import shadow.utils.objects.savable.data.gui.virtual.VirtualInventory;
import shadow.utils.users.types.UnverifiedUser;

import static shadow.utils.main.AlixUtils.maxLoginAttempts;

public final class VirtualAnvilPasswordBuilder extends AnvilBuilderBase implements AlixJavaVerificationGui {

   /* private static final EmptyVirtualInventory
            registerGUI,
            loginGUI;*/
    private static final boolean invisible = true;
    private final EmptyVirtualInventory gui;
    private final UnverifiedUser user;

    public VirtualAnvilPasswordBuilder(UnverifiedUser user) {
        super(user.silentContext(), user.isRegistered(), invisible ? AnvilPacketSuppliers.newUnverifiedInvisibleSupplier() : AnvilPacketSuppliers.unverifiedVisibleSupplier());
        this.user = user;
        this.gui = new EmptyVirtualInventory(user.silentContext(), AlixInventoryType.ANVIL, user.isRegistered() ? PasswordGui.guiTitleLogin : PasswordGui.guiTitleRegister);
        //this.updateWindowId(1); <- broken
    }

    @Override
    public void openGUI() {
        AlixJavaVerificationGui.super.openGUI();
        this.updateWindowId(1);
    }

    @Override
    @NotNull
    public VirtualInventory getVirtualGUI() {
        return this.gui;
    }

    @Override
    public void select(int slot) {
        switch (slot) {
            case 1:
                MethodProvider.kickAsync(user, pinLeaveFeedbackKickPacket);
                break;
            case 2:
                String password = this.getPasswordBuilt();
                if (password.isEmpty()) return;
                if (user.isRegistered()) {
                    if (user.isPasswordCorrect(password)) {
                        this.user.tryLogIn();
                        return;
                    } else if (++user.loginAttempts == maxLoginAttempts)
                        MethodProvider.kickAsync(this.user, CommandManager.incorrectPasswordKickPacket);
                    return;
                }
                String reason = AlixUtils.getInvalidityReason(password, false);
                if (reason != null) {
                    this.user.writeConstSilently(PasswordGui.villagerNoSoundPacket);
                    this.user.sendDynamicMessageSilently(reason);
                    return;
                }
                this.user.writeConstSilently(CommandManager.passwordRegisterMessagePacket);
                this.user.registerAsync(password);//invokes flush
        }
    }

    @Override
    public LoginType getType() {
        return LoginType.ANVIL;
    }

    @NotNull
    @Override
    public String getPasswordBuilt() {
        return password;
    }
}