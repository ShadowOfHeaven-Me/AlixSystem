package shadow.utils.objects.savable.data.gui.bedrock;

import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.messages.Messages;
import alix.common.utils.other.throwable.AlixException;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.NotNull;
import shadow.systems.commands.CommandManager;
import shadow.utils.objects.savable.data.gui.AlixBedrockVerificationGui;
import shadow.utils.users.types.UnverifiedUser;

import static shadow.systems.commands.CommandManager.registerPasswordsDoNotMatchMessagePacket;
import static shadow.utils.main.AlixUtils.requirePasswordRepeatInRegister;

public final class VerificationBedrockGUI implements AlixBedrockVerificationGui {

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
    private final UnverifiedUser user;
    private final FloodgatePlayer player;
    private final CustomForm form;

    //Source code: https://github.com/Null-K/KcLoginGui_En/blob/ma
    // 3,ster/src/main/java/com/puddingkc/KcLoginGui.java ;]
    public VerificationBedrockGUI(UnverifiedUser user, Object bedrockPlayer) {
        this.user = user;
        this.player = (FloodgatePlayer) bedrockPlayer; //FloodgateApi.getInstance().getPlayer(this.user.getPlayer().getUniqueId());
        this.form = user.isRegistered() ? constructLogin(this) : constructRegister(this);
        //this.user.setGUIInitialized(true);
    }

    private void inputRegister(CustomFormResponse response) {
        String arg1 = response.asInput(0);

        this.register0(arg1, response.asToggle(1));
    }

    private void inputRegisterRepeat(CustomFormResponse response) {
        String arg1 = response.asInput(0);
        String arg2 = response.asInput(1);

        if (!arg1.equals(arg2)) {
            this.user.writeAndFlushConstSilently(registerPasswordsDoNotMatchMessagePacket);
            this.open();
            return;
        }
        this.register0(arg1, response.asToggle(2));
    }

    private void register0(String password, boolean autoLogin) {
        if (CommandManager.tryRegisterIfValid(this.user, password)) {
            PersistentUserData data = UserFileManager.get(this.user.getPlayer().getName());//since UnverifiedUser#getData is null
            if (data == null) throw new AlixException("Null data after bedrock register! Report this immediately!");
            data.getLoginParams().setIpAutoLogin(autoLogin);
        } else this.open();//invalid password
    }

    private void inputLogin(CustomFormResponse response) {
        String arg1 = response.asInput(0);
        boolean autoLogin = response.asToggle(1);

        if (CommandManager.onAsyncLoginCommand(this.user, arg1)) {
            this.user.getData().getLoginParams().setIpAutoLogin(autoLogin);
        } else this.open();//incorrect password
    }

    private static CustomForm constructRegister(VerificationBedrockGUI gui) {
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

    private static CustomForm constructLogin(VerificationBedrockGUI gui) {
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
    public void openGUI() {
        this.open();
    }

    @Override
    @NotNull
    public LoginType getType() {
        return LoginType.BEDROCK_VER;
    }
}