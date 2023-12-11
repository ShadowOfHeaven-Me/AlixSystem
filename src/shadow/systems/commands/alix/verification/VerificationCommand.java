package shadow.systems.commands.alix.verification;

import org.bukkit.entity.Player;
import shadow.systems.commands.CommandManager;
import shadow.utils.users.offline.UnverifiedUser;

@FunctionalInterface
public interface VerificationCommand {

    void onCommand(UnverifiedUser user, Player player, String arg2);

    VerificationCommand OF_CAPTCHA = CommandManager::onCaptchaCommand;
    VerificationCommand OF_REGISTER = CommandManager::onRegisterCommand;
    VerificationCommand OF_LOGIN = CommandManager::onLoginCommand;
}