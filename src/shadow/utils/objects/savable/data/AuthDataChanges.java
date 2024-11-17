package shadow.utils.objects.savable.data;

import alix.common.data.AuthSetting;
import alix.common.data.LoginParams;
import alix.libs.com.github.retrooper.packetevents.protocol.sound.Sounds;
import io.netty.buffer.ByteBuf;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.objects.savable.data.gui.builders.auth.VerifiedVirtualAuthBuilder;
import shadow.utils.users.types.VerifiedUser;

import static alix.common.messages.Messages.getWithPrefix;

public final class AuthDataChanges {

    private static final ByteBuf appliedChangesMessagePacket = OutMessagePacketConstructor.constructConst(getWithPrefix("gui-google-auth-applied-changes"));
    private AuthSetting authSetting;

    public AuthDataChanges() {
    }

    public void tryApply(VerifiedUser user) {
        if (authSetting == null) return;
        LoginParams params = user.getData().getLoginParams();

        if (params.hasProvenAuthAccess()) {
            this.apply0(user);
            VerifiedVirtualAuthBuilder.send(Sounds.ENTITY_PLAYER_LEVELUP, user, VerifiedVirtualAuthBuilder.vec3iLoc(user));
            user.getPlayer().closeInventory();
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

        user.writeAndFlushConstSilently(appliedChangesMessagePacket);
        //VerifiedVirtualAuthBuilder.
        params.setAuthSettings(authSetting);
    }

    public void setAuthSetting(AuthSetting authSetting) {
        this.authSetting = authSetting;
    }
}