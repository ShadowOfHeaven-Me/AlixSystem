package shadow.systems.commands.alix.verification;

import shadow.systems.commands.CommandManager;
import shadow.utils.objects.packet.types.unverified.PacketBlocker;
import shadow.utils.users.offline.UnverifiedUser;

@FunctionalInterface
public interface VerificationCommand {

    void onCommand(UnverifiedUser user, String arg1);

    VerificationCommand OF_CAPTCHA = CommandManager::onAsyncCaptchaCommand; //PacketBlocker.serverboundNameVersion ? CommandManager::onAsyncCaptchaCommand : CommandManager::onSyncCaptchaCommand;
    VerificationCommand OF_REGISTER = PacketBlocker.serverboundNameVersion ? CommandManager::onAsyncRegisterCommand : CommandManager::onSyncRegisterCommand;
    VerificationCommand OF_LOGIN = PacketBlocker.serverboundNameVersion ? CommandManager::onAsyncLoginCommand : CommandManager::onSyncLoginCommand;
}