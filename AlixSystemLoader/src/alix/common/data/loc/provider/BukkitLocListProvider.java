package alix.common.data.loc.provider;

import alix.common.data.loc.AlixLocationList;
import alix.common.data.loc.impl.bukkit.BukkitHomeList;

final class BukkitLocListProvider implements LocationListProvider {

    BukkitLocListProvider() {
    }

    @Override
    public AlixLocationList fromSavable(String line) {
        return new BukkitHomeList(line);
    }

    @Override
    public AlixLocationList newList() {
        return new BukkitHomeList();
    }
}