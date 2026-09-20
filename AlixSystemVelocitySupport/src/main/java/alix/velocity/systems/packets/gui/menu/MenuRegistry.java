package alix.velocity.systems.packets.gui.menu;

import alix.common.AlixCommonMain;
import alix.velocity.systems.packets.gui.impl.AccountGUI;
import alix.velocity.systems.packets.gui.impl.GoogleAuthGUI;
import alix.velocity.systems.packets.gui.impl.IpAutoLoginGUI;
import alix.velocity.systems.packets.gui.impl.LoginSettingsGUI;
import alix.velocity.systems.packets.gui.impl.PasswordsGUI;
import alix.velocity.utils.user.VerifiedUser;

/**
 * Resolves the menu ids used by "[open-menu] &lt;id&gt;" actions in gui-menus/*.yml files to the actual
 * menu classes. "user.gui" (the menu the player currently has open) is used as the "go back to" target
 * for sub-menus, matching how these menus were already opened from one another before this system existed.
 */
public final class MenuRegistry {

    public static void open(String id, VerifiedUser user) {
        if (id == null || id.isBlank()) return;

        switch (id.trim().toLowerCase()) {
            case "account":
                AccountGUI.add(user);
                return;
            case "passwords":
                PasswordsGUI.add(user, user.gui);
                return;
            case "login-settings":
                LoginSettingsGUI.add(user, user.gui);
                return;
            case "google-auth":
                GoogleAuthGUI.add(user, user.gui);
                return;
            case "ip-autologin":
                IpAutoLoginGUI.add(user);
                return;
            default:
                AlixCommonMain.logWarning("Menu action \"[open-menu] " + id + "\" references an unknown menu id " +
                        "(valid: account, passwords, login-settings, google-auth, ip-autologin)");
        }
    }

    private MenuRegistry() {
    }
}
