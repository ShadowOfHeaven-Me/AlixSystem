package alix.velocity.systems.packets.gui.changes;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.common.messages.Messages;
import alix.common.utils.formatter.AlixFormatter;
import alix.velocity.utils.user.VerifiedUser;

import static alix.common.messages.Messages.getWithPrefix;

public final class AuthDataChanges {

    private static final String appliedChangesMessage = getWithPrefix("gui-google-auth-applied-changes");
    private AuthSetting authSetting;

    public AuthDataChanges() {
    }

    public void tryApply(VerifiedUser user) {
        if (authSetting == null) return;
        LoginParams params = user.getData().getLoginParams();

        if (params.hasProvenAuthAccess()) {
            this.apply0(user);
            //VerifiedVirtualAuthBuilder.send(Sounds.ENTITY_PLAYER_LEVELUP, user, VerifiedVirtualAuthBuilder.vec3iLoc(user));
            //user.getPlayer().closeInventory();
            user.closeInventory();
            return;
        }
        switch (authSetting) {
            case AUTH_APP:
            case PASSWORD_AND_AUTH_APP:
                user.getDuplexProcessor().verifyAuthAccess(() -> this.apply0(user));
        }
    }

    private void apply0(VerifiedUser user) {
        LoginParams params = user.getData().getLoginParams();
        boolean wasRequired = requiresApp(params.getAuthSettings());

        user.user.sendMessage(appliedChangesMessage);
        //VerifiedVirtualAuthBuilder.
        params.setAuthSettings(authSetting);

        //The app is newly becoming required (it wasn't a moment ago) - generate this account's first set
        //of recovery codes right now, same as a "Reset Code" does, rather than leaving the player with
        //none until they happen to click "Recovery Codes" or "Reset Code" separately - see the Spigot
        //AuthDataChanges' equivalent for the full reasoning.
        if (!wasRequired && requiresApp(this.authSetting)) {
            String[] codes = user.getData().regenerateRecoveryCodes();
            sendRecoveryCodes(user, codes);
        }
    }

    private static boolean requiresApp(AuthSetting setting) {
        return setting == AuthSetting.AUTH_APP || setting == AuthSetting.PASSWORD_AND_AUTH_APP;
    }

    private static void sendRecoveryCodes(VerifiedUser user, String[] codes) {
        user.user.sendMessage(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-header"));
        for (String code : codes) {
            user.user.sendMessage(AlixFormatter.translateColors("&e" + code));
        }
        user.user.sendMessage(Messages.getWithPrefix("gui-google-auth-recovery-codes-chat-footer"));
    }

    public void setAuthSetting(AuthSetting authSetting) {
        this.authSetting = authSetting;
    }
}