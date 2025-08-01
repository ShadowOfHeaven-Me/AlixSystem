package alix.velocity.systems.packets.gui.changes;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
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

        user.user.sendMessage(appliedChangesMessage);
        //VerifiedVirtualAuthBuilder.
        params.setAuthSettings(authSetting);
    }

    public void setAuthSetting(AuthSetting authSetting) {
        this.authSetting = authSetting;
    }
}