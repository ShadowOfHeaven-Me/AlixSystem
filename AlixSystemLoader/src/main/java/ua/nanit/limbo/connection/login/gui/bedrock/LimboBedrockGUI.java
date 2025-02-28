package ua.nanit.limbo.connection.login.gui.bedrock;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.login.LoginState;
import ua.nanit.limbo.connection.login.gui.LimboGUI;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;

import static ua.nanit.limbo.connection.login.LoginState.registerPasswordsDoNotMatchMessagePacket;
import static ua.nanit.limbo.connection.login.LoginState.requirePasswordRepeatInRegister;

public final class LimboBedrockGUI implements LimboGUI {

    private static final String
            loginTitle = Messages.get("bedrock-login-title"),
            registerTitle = Messages.get("bedrock-register-title"),
            aboveInputRegister = Messages.get("bedrock-register-above-input"),
            aboveInputRegisterRepeat = Messages.get("bedrock-register-above-input-repeat"),
            aboveInputLogin = Messages.get("bedrock-login-above-input"),
            loginInput = Messages.get("bedrock-login-input"),
            registerInput = Messages.get("bedrock-register-input"),
            registerInputRepeat = Messages.get("bedrock-register-input-repeat"),
            rememberMe = Messages.get("bedrock-remember-me");
    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;
    private final FloodgatePlayer player;
    private final CustomForm form;
    private final LoginState loginState;

    //Source code: https://github.com/Null-K/KcLoginGui_En/blob/master/src/main/java/com/puddingkc/KcLoginGui.java ;]
    public LimboBedrockGUI(ClientConnection connection, Object bedrockPlayer, LoginState loginState) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        this.player = (FloodgatePlayer) bedrockPlayer;
        this.loginState = loginState;
        this.form = loginState.isRegistered ? constructLogin(this) : constructRegister(this);
    }

    private void inputRegister(CustomFormResponse response) {
        String arg1 = response.asInput(0);

        this.register0(arg1, response.asToggle(1));
    }

    private void inputRegisterRepeat(CustomFormResponse response) {
        String arg1 = response.asInput(0);
        String arg2 = response.asInput(1);

        if (!arg1.equals(arg2)) {
            this.duplexHandler.write(registerPasswordsDoNotMatchMessagePacket);
            this.open();
            return;
        }
        this.register0(arg1, response.asToggle(2));
    }

    private void register0(String password, boolean autoLogin) {
        this.connection.getChannel().eventLoop().execute(() -> {
            var data = this.loginState.registerIfValid(password, LoginType.BEDROCK_VER);
            if (data != null) {
                //valid password
                data.getLoginParams().setIpAutoLogin(autoLogin);
            } else this.open();//invalid password
        });
    }

    private void inputLogin(CustomFormResponse response) {
        String arg1 = response.asInput(0);
        boolean autoLogin = response.asToggle(1);

        this.connection.getChannel().eventLoop().execute(() -> {
            if (this.loginState.isPasswordCorrect(arg1)) {
                this.loginState.data.getLoginParams().setIpAutoLogin(autoLogin);
                this.loginState.logIn();
                return;
            }
            if (this.loginState.onIncorrectPassword())
                this.open();
        });
    }

    private static CustomForm constructRegister(LimboBedrockGUI gui) {
        if (requirePasswordRepeatInRegister) {
            return CustomForm.builder()
                    .title(registerTitle)
                    .input(aboveInputRegister, registerInput)
                    .input(aboveInputRegisterRepeat, registerInputRepeat)
                    .toggle(rememberMe, false)
                    .validResultHandler(gui::inputRegisterRepeat)
                    .closedOrInvalidResultHandler(gui::open)
                    .build();
        }
        return CustomForm.builder()
                .title(registerTitle)
                .input(aboveInputRegister, registerInput)
                .toggle(rememberMe, false)
                .validResultHandler(gui::inputRegister)
                .closedOrInvalidResultHandler(gui::open)
                .build();
    }

    private static CustomForm constructLogin(LimboBedrockGUI gui) {
        return CustomForm.builder()
                .title(loginTitle)
                .input(aboveInputLogin, loginInput)
                .toggle(rememberMe, false)
                .validResultHandler(gui::inputLogin)
                .closedOrInvalidResultHandler(gui::open)
                .build();
    }

    private void open() {
        this.player.sendForm(this.form);
    }

    @Override
    public void select(int slot) {//obsolete
    }

    @Override
    public void onCloseAttempt() {//obsolete
    }

    @Override
    public void show() {
        this.open();
    }
}