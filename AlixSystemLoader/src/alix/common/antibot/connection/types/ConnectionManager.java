package alix.common.antibot.connection.types;

import alix.common.antibot.connection.ConnectionFilter;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.collections.list.LoopList;

public class ConnectionManager implements ConnectionFilter {

    private final String s = Messages.get("prevent-first-time-join");
    private final LoopList<String> list;

    public ConnectionManager() {
        short maxSize = (short) Math.max(Math.min((short) ConfigProvider.config.getInt("connection-list-size"), 32767), 3);
        this.list = new LoopList<>(new String[maxSize]);
    }

    @Override
    public boolean disallowJoin(String address, String name) {
        boolean notPresent = !list.contains(name);
        if (notPresent) {
            list.setNext(name);
            return true;
        }
        return false;
    }

    @Override
    public String getReason() {
        return s;
/*        return JavaUtils.isPluginLanguageEnglish ? "We're analysing your connection. You may now join the server." :
                "Analizujemy twoje połączenie. Możesz już dołączyć do serwera.";*/
    }
}