package shadow.utils.objects.savable.data;

import alix.common.data.PersistentUserData;
import alix.common.login.auth.AbstractDataChanges;
import org.bukkit.entity.Player;

public final class DataChanges extends AbstractDataChanges<Player> {

    public DataChanges(PersistentUserData data) {
        super(data, Player::sendRawMessage);
    }
}