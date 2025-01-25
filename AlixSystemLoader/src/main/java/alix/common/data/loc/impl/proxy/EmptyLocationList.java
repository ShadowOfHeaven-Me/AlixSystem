package alix.common.data.loc.impl.proxy;

import alix.common.data.loc.AlixLocationList;

public final class EmptyLocationList implements AlixLocationList {

    public static final EmptyLocationList INSTANCE = new EmptyLocationList();

    private EmptyLocationList() {
    }

    @Override
    public String toSavable() {
        return "0";
    }
}