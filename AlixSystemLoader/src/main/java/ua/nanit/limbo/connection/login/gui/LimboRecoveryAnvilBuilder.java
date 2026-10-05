package ua.nanit.limbo.connection.login.gui;

import alix.common.data.security.email.EmailHandler;
import alix.common.messages.Messages;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.protocol.packets.play.PacketPlayOutMessage;

public final class LimboRecoveryAnvilBuilder extends AbstractAnvilBuilder<LimboRecoveryAnvilBuilder> implements LimboGUI {

    private final ClientConnection connection;
    private final LoginState loginState;
    private final boolean isCodePhase;

    public LimboRecoveryAnvilBuilder(ClientConnection connection, LoginState loginState, boolean isCodePhase) {
        super(connection.getChannel(), connection.getClientVersion(), isCodePhase ? AnvilBuilderGoal.RECOVERY_CODE : AnvilBuilderGoal.RECOVERY_EMAIL, self -> self.connection.getDuplexHandler().flush());
        this.connection = connection;
        this.loginState = loginState;
        this.isCodePhase = isCodePhase;
    }

    @Override
    public void select(int slot) {
        switch (slot) {
            case 0:
                this.spoofValidAccordingly();
                return;
            case 1:
                this.loginState.reopenOriginalGui();
                return;
            case 2:
                String text = this.input.trim();
                if (text.isEmpty()) {
                    this.spoofValidAccordingly();
                    return;
                }

                if (!isCodePhase) {
                    if (this.loginState.data != null && this.loginState.data.canUseEmailRecovery()) {
                        String registeredEmail = this.loginState.data.getEmail().email();
                        if (text.equalsIgnoreCase(registeredEmail)) {
                            EmailHandler.sendRecoveryMail(this.connection, text, (conn, msg) -> conn.getDuplexHandler().writeAndFlush(PacketPlayOutMessage.withMessage(msg)));
                            this.loginState.openRecoveryCodeGui();
                            return;
                        }
                    }
                    //see LoginState#registerInvalidRecoveryEmailAttempt()'s own docs - without this, the GUI
                    //path let an attacker guess indefinitely with no lockout, unlike the equivalent chat command.
                    if (this.loginState.registerInvalidRecoveryEmailAttempt()) return;
                    this.loginState.writeMessage(Messages.get("email-recovery-invalid-email"));
                    this.spoofValidAccordingly();
                } else {
                    if (EmailHandler.verifyRecoveryCode(this.connection, text)) {
                        this.loginState.writeMessage(Messages.get("email-recovery-success"));
                        this.loginState.tryLogIn();
                    } else {
                        if (this.loginState.registerInvalidRecoveryCodeAttempt()) return;
                        this.loginState.writeMessage(Messages.get("email-recovery-code-invalid"));
                        this.spoofValidAccordingly();
                    }
                }
        }
    }

    @Override
    public void onCloseAttempt() {
        this.show();
    }

    @Override
    public void show() {
        this.open();
    }
}
