package alix.common.data.loc.provider;

import alix.common.data.loc.AlixLocationList;
import alix.common.data.loc.impl.proxy.EmptyLocationList;

final class ProxyLocListProvider implements LocationListProvider {

    @Override
    public AlixLocationList fromSavable(String line) {
        return EmptyLocationList.INSTANCE;
    }

    @Override
    public AlixLocationList newList() {
        return EmptyLocationList.INSTANCE;
    }
}