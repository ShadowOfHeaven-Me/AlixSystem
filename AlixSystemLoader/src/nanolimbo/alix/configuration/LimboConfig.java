/*
 * Copyright (C) 2020 Nan1t
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package nanolimbo.alix.configuration;


import nanolimbo.alix.server.data.InfoForwarding;
import nanolimbo.alix.server.data.PingData;
import nanolimbo.alix.server.data.Title;
import nanolimbo.alix.util.Colors;
import nanolimbo.alix.util.NbtMessageUtil;

public final class LimboConfig {

    private final int maxPlayers;
    private final PingData pingData;

    private final String dimensionType;
    private final int gameMode;

    private final String brandName;
    private final String joinMessage;
    private final Title title;

    private final String playerListHeader;
    private final String playerListFooter;

    private final InfoForwarding infoForwarding;
    private final long readTimeout;

    private final boolean useTrafficLimits;
    private final int maxPacketSize;
    private final double interval;
    private final double maxPacketRate;

    public LimboConfig() {
        maxPlayers = 20;
        pingData = new PingData("Ping Sex", "{\"text\": \"&9NanoLimbo\"}", -1);
        dimensionType = "the_end";
        gameMode = 3;

        brandName = "AlixVirtualLimbo";
        joinMessage = Colors.of("{\"text\": \"&eWelcome to the Limbo!\"}");
        title = new Title();
        title.setTitle(NbtMessageUtil.create(Colors.of("{\"text\": \"&9&lHehehe!\"}")));
        title.setSubtitle(NbtMessageUtil.create(Colors.of("{\"text\": \"&9&lWelcome!\"}")));
        title.setFadeIn(0);
        title.setStay(100);
        title.setFadeOut(0);

        playerListHeader = Colors.of("none");
        playerListFooter = Colors.of("none");

        infoForwarding = new InfoForwarding(InfoForwarding.Type.NONE, null, null);
        readTimeout = 30L;

        useTrafficLimits = false;
        maxPacketSize = -1;
        interval = -1;
        maxPacketRate = -1;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public PingData getPingData() {
        return pingData;
    }

    public String getDimensionType() {
        return dimensionType;
    }

    public int getGameMode() {
        return gameMode;
    }

    public InfoForwarding getInfoForwarding() {
        return infoForwarding;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public boolean isUseBrandName() {
        return true;
    }

    public boolean isUseJoinMessage() {
        return true;
    }

    public boolean isUseBossBar() {
        return false;
    }

    public boolean isUseTitle() {
        return true;
    }

    public boolean isUsePlayerList() {
        return false;
    }

    public boolean isUseHeaderAndFooter() {
        return false;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public Title getTitle() {
        return title;
    }

    public String getPlayerListUsername() {
        return "xes";
    }

    public String getPlayerListHeader() {
        return playerListHeader;
    }

    public String getPlayerListFooter() {
        return playerListFooter;
    }

    public boolean isUseTrafficLimits() {
        return useTrafficLimits;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public double getInterval() {
        return interval;
    }

    public double getMaxPacketRate() {
        return maxPacketRate;
    }
}
