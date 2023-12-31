package shadow.utils.objects.savable.data.gui.bases;

import shadow.systems.commands.CommandManager;
import shadow.utils.holders.methods.MethodProvider;
import shadow.utils.objects.savable.data.gui.AlixGui;
import alix.common.messages.Messages;
import alix.common.messages.AlixMessage;
import shadow.utils.users.offline.UnverifiedUser;

import static shadow.systems.commands.CommandManager.incorrectPassword;
import static shadow.systems.commands.CommandManager.loginSuccess;
import static shadow.utils.main.AlixUtils.kickOnIncorrectPassword;
import static shadow.utils.main.AlixUtils.sendMessage;

public abstract class PasswordBuilderBase implements AlixGui {

    private static final String pinRegister = Messages.get("pin-register");
    private static final AlixMessage pinRegisterBottomLine = Messages.getAsObject("pin-register-bottom-line");
    protected final UnverifiedUser user;

    public PasswordBuilderBase(UnverifiedUser user) {
        this.user = user;
    }

    public void onPINConfirmation() {
        String pin = this.getPasswordBuilt();

        if (user.isRegistered()) {
            if (user.isPasswordCorrect(pin)) {
                user.logInSync();
                sendMessage(user.getPlayer(), loginSuccess);
                return;
            } else if (kickOnIncorrectPassword) MethodProvider.kickAsync(user, CommandManager.incorrectPasswordKickPacket);
            return;
        }
        user.registerSync(pin);
        user.getPlayer().sendTitle(pinRegister, pinRegisterBottomLine.format(pin), 0, 100, 50);
    }
}