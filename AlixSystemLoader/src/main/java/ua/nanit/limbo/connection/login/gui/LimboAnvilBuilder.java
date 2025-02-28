package ua.nanit.limbo.connection.login.gui;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;

import static ua.nanit.limbo.connection.login.gui.LimboAuthBuilder.leaveFeedbackKickPacket;

public final class LimboAnvilBuilder extends AbstractAnvilBuilder<LimboAnvilBuilder> implements LimboGUI {

    public static final ItemStack GO_BACK_ITEM = AbstractAnvilBuilder.GO_BACK_ITEM;
    private final ClientConnection connection;
    private final LoginState loginState;
    private final boolean isRegistered;

    public LimboAnvilBuilder(ClientConnection connection, PersistentUserData data, LoginState loginState, AnvilBuilderGoal goal) {
        super(connection.getChannel(), connection.getClientVersion(), goal, self -> self.connection.getDuplexHandler().flush());
        this.connection = connection;
        this.isRegistered = PersistentUserData.isRegistered(data);
        this.loginState = loginState;
    }

    @Override
    public void select(int slot) {
        switch (slot) {
            case 0: {
                this.spoofValidAccordingly();
                return;
            }
            case 1:
                this.connection.sendPacketAndClose(leaveFeedbackKickPacket);
                return;
            case 2:
                String password = this.input;
                if (password.isEmpty()) {
                    this.spoofValidAccordingly();
                    return;
                }
                if (this.isRegistered) {
                    if (this.loginState.isPasswordCorrect(password)) {
                        this.loginState.tryLogIn();
                        return;
                    }
                    if (this.loginState.onIncorrectPassword())
                        this.spoofValidAccordingly();
                    return;
                }
                this.loginState.registerIfValid(password, LoginType.ANVIL);
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