package shadow.systems.commands.alix.verification;

import shadow.systems.commands.CommandManager;
import shadow.utils.users.types.UnverifiedUser;

public interface VerificationCommand {

    void onCommand(UnverifiedUser user, String arg1);

    //VerificationCommand OF_CAPTCHA = CommandManager::onAsyncCaptchaCommand; //PacketBlocker.serverboundNameVersion ? CommandManager::onAsyncCaptchaCommand : CommandManager::onSyncCaptchaCommand;
    VerificationCommand OF_REGISTER = CommandManager::onAsyncRegisterCommand; //PacketBlocker.serverboundChatCommandPacketVersion ? CommandManager::onAsyncRegisterCommand : CommandManager::onSyncRegisterCommand;
    VerificationCommand OF_LOGIN = CommandManager::onAsyncLoginCommand; //PacketBlocker.serverboundChatCommandPacketVersion ? CommandManager::onAsyncLoginCommand : CommandManager::onSyncLoginCommand;
}