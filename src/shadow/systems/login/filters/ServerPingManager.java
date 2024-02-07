package shadow.systems.login.filters;

import alix.common.messages.Messages;
import alix.common.utils.config.ConfigProvider;
import alix.common.utils.collections.list.LoopList;

public final class ServerPingManager implements ConnectionFilter {

    public static final ServerPingManager INSTANCE = ConfigProvider.config.getBoolean("ping-before-join") ? new ServerPingManager() : null;
    private static final boolean registered = INSTANCE != null;
    private final String s = Messages.get("ping-before-join");
    private final LoopList<String> list;

    public ServerPingManager() {
        short pingListSize = (short) Math.max(Math.min((short) ConfigProvider.config.getInt("ping-list-size"), 32767), 3);
        this.list = LoopList.newConcurrent(new String[pingListSize]);
        //Main.debug("Initializing ServerPingManager...");
    }

    public static void init() {
        //Main.debug("Initialized ServerPingManager!");
    }

    public static void pureAdd(String address) {
        INSTANCE.addIfAbsent(address);
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static String[] getUserReadablePings() {
        return INSTANCE.getCosmeticPings();
    }

    public void addIfAbsent(String address) {
        if (!list.contains(address)) list.setNext(address);
    }

    public String[] getCosmeticPings() {
        int till = tillNull();
        String[] existingPings = new String[till];
        System.arraycopy(list.getValues(), 0, existingPings, 0, till);
        return existingPings;
    }

    private int tillNull() {
        for (byte i = 0; i < list.size(); i++) if (list.get(i) == null) return i;
        return list.size();
    }

    @Override
    public boolean disallowJoin(String address, String name) {
        return !list.contains(address);
    }

    @Override
    public String getReason() {
        return s;
/*        return isPluginLanguageEnglish ? "You must first add this server to you server list and refresh it!" :
                "Najpierw dodaj ten serwer do swojej listy serwerów i ją odśwież!";*/
    }

/*
    public void clear() {
        for (byte i = 0; i < pingListSize; i++) {
            pings[i] = null;
        }
        index = 0;
    }*/
}