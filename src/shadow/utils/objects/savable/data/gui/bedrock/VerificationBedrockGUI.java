package shadow.utils.objects.savable.data.gui.bedrock;

import alix.common.data.LoginType;
import alix.common.messages.Messages;
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
            registerInputRepeat = Messages.get("bedrock-register-input-repeat");
    private final UnverifiedUser user;
    private final FloodgatePlayer player;
    private final CustomForm form;

    //Source code: https://github.com/Null-K/KcLoginGui_En/blob/master/src/main/java/com/puddingkc/KcLoginGui.java ;]
    public VerificationBedrockGUI(UnverifiedUser user, Object bedrockPlayer) {
        this.user = user;
        this.player = (FloodgatePlayer) bedrockPlayer; //FloodgateApi.getInstance().getPlayer(this.user.getPlayer().getUniqueId());
        this.form = user.isRegistered() ? constructLogin(this) : constructRegister(this);
        //this.user.setGUIInitialized(true);
    }

    private void inputRegister(CustomFormResponse response) {
        String arg1 = response.asInput(0);
        if (requirePasswordRepeatInRegister) {
            String arg2 = response.asInput(1);
            if (!arg1.equals(arg2)) {
                this.user.writeAndFlushConstSilently(registerPasswordsDoNotMatchMessagePacket);
                this.open();
                return;
            }
        }
        if (!CommandManager.tryRegisterIfValid(this.user, arg1)) this.open();
    }

    private void inputLogin(CustomFormResponse response) {
        String arg1 = response.asInput(0);
        if (!CommandManager.onAsyncLoginCommand(this.user, arg1)) this.open();
    }

    public void open() {
        this.player.sendForm(this.form);
    }

    private static CustomForm constructRegister(VerificationBedrockGUI gui) {
        if (requirePasswordRepeatInRegister) {
            return CustomForm.builder()
                    .title(registerTitle)
                    .input(aboveInputRegister, registerInput)
                    .input(aboveInputRegisterRepeat, registerInputRepeat)
                    .validResultHandler(gui::inputRegister)
                    .closedOrInvalidResultHandler(gui::open)
                    .build();
        }
        return CustomForm.builder()
                .title(registerTitle)
                .input(aboveInputRegister, registerInput)
                .validResultHandler(gui::inputRegister)
                .closedOrInvalidResultHandler(gui::open)
                .build();
    }

    private static CustomForm constructLogin(VerificationBedrockGUI gui) {
        return CustomForm.builder()
                .title(loginTitle)
                .input(aboveInputLogin, loginInput)
                .validResultHandler(gui::inputLogin)
                .closedOrInvalidResultHandler(gui::open)
                .build();
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