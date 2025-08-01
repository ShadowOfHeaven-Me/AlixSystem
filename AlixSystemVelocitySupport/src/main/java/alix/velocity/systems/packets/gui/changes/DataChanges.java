package alix.velocity.systems.packets.gui.changes;

import alix.common.data.PersistentUserData;
import alix.common.login.auth.AbstractDataChanges;
import com.github.retrooper.packetevents.protocol.player.User;

public final class DataChanges extends AbstractDataChanges<User> {

    public DataChanges(PersistentUserData data) {
        super(data, User::sendMessage);
    }
}