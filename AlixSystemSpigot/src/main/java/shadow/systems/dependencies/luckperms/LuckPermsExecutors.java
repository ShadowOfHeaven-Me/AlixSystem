/*
package shadow.systems.dependencies.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.event.Listener;
import shadow.utils.users.types.VerifiedUser;
import shadow.utils.users.UserManager;

public class LuckPermsExecutors implements Listener {

    public static void register() {
        LuckPerms api = LuckPermsProvider.get();
        EventBus bus = api.getEventBus();

        bus.subscribe(UserDataRecalculateEvent.class, event -> {
            VerifiedUser user = UserManager.getNullableVerifiedUser(event.getUser().getUniqueId());

            //event.getData().getPermissionData().getPermissionMap()

            //event.getData().getMetaData().getMetaValue(
        });
    }
}*/
