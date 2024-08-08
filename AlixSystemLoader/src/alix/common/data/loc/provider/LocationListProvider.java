package alix.common.data.loc.provider;

import alix.common.data.loc.AlixLocationList;
import alix.common.environment.ServerEnvironment;

public interface LocationListProvider {

    AlixLocationList fromSavable(String line);

    AlixLocationList newList();

    static LocationListProvider createImpl() {
        switch (ServerEnvironment.getEnvironment()) {
            case SPIGOT:
            case PAPER:
                return new BukkitLocListProvider();
            default:
                return new ProxyLocListProvider();
        }
    }
}