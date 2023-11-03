/*
package shadow.utils.users.offline;

import shadow.systems.login.LoginProcess;
import shadow.utils.objects.savable.data.PersistentUserData;
import org.bukkit.entity.Player;

public class TeleportUser extends UnverifiedUser {

    private final LoginProcess process;

    public TeleportUser(Player p, PersistentUserData data) {
        super(p, data);
        process = new LoginProcess(this);
    }

    @Override
    public void disableActionBlocker() {
        returnToOriginalSelf();
        process.end();
    }
}*/
