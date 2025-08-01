package alix.velocity.systems.packets.gui.inv;

import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;

public interface AbstractInventory {

    default PersistentUserData getData() {
        return UserFileManager.get(this.getUser().getName());
    }

    void open();

    VerifiedUser getUser();

    void setItem(int i, ItemStack item);

    void spoofItems();

    int getSize();
}