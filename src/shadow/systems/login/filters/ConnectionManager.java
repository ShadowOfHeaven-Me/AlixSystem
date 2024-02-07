package shadow.systems.login.filters;

import alix.common.messages.Messages;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.collections.list.LoopList;

public final class ConnectionManager implements ConnectionFilter {

    private final String s = Messages.get("prevent-first-time-join");
    private final LoopList<String> list;

    public ConnectionManager() {
        short maxSize = (short) Math.max(Math.min((short) ConfigProvider.config.getInt("connection-list-size"), 32767), 3);
        this.list = LoopList.newConcurrent(new String[maxSize]);
    }

    @Override
    public boolean disallowJoin(String address, String name) {
        if (!list.contains(name)) {
            list.setNext(name);
            return true;
        }
        return false;
    }

    @Override
    public String getReason() {
        return s;
    }
}