package ua.nanit.limbo.configuration;


import alix.common.messages.Messages;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import ua.nanit.limbo.server.data.Title;
import ua.nanit.limbo.util.Colors;
import ua.nanit.limbo.world.DimensionType;

import static ua.nanit.limbo.connection.login.LoginState.requirePasswordRepeatInRegister;

public final class LimboConfig {

    private final int maxPlayers;

    private final GameMode gameMode;

    private final String brandName;
    private final String joinMessage;
    private final Title loginTitle, registerTitle, termsTitle;

    private final String playerListHeader;
    private final String playerListFooter;

    private final long readTimeout;

    private final boolean useTrafficLimits;
    private final int maxPacketSize;
    private final double interval;
    private final double maxPacketRate;

    public LimboConfig() {
        maxPlayers = 500;
        gameMode = GameMode.ADVENTURE;

        brandName = "AlixVirtualLimbo";
        joinMessage = Colors.of("{\"text\": \"&eWelcome to the Limbo!\"}");

        int registerStayTicks = 999999999;
        int loginStayTicks = 999999999;
        registerTitle = new Title().setTitle(Messages.get("reminder-register-title")).setSubtitle(requirePasswordRepeatInRegister ? Messages.get("reminder-register-subtitle-repeat") : Messages.get("reminder-register-subtitle")).setStay(registerStayTicks);
        loginTitle = new Title().setTitle(Messages.get("reminder-login-title")).setSubtitle(Messages.get("reminder-login-subtitle")).setStay(loginStayTicks);
        //Shown instead of registerTitle while an unregistered player still hasn't accepted the Terms & Conditions
        //(see LoginState#isTermsGateBlocking()) - otherwise the title kept telling players to "Register with
        ///register <password>" even while registration was actually still gated behind "/terms accept" first.
        termsTitle = new Title().setTitle(Messages.get("reminder-terms-title")).setSubtitle(Messages.get("reminder-terms-subtitle")).setStay(registerStayTicks);

        playerListHeader = Colors.of("none");
        playerListFooter = Colors.of("none");

        readTimeout = 30L;

        useTrafficLimits = false;
        maxPacketSize = -1;
        interval = -1;
        maxPacketRate = -1;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getDimensionTypeName() {
        return "the_end";
    }

    public DimensionType getDimensionType() {
        return DimensionType.THE_END;
    }

    /*public DimensionType getDimensionType(Version version) {
        return version.moreOrEqual(Version.V1_21_9) ? DimensionTypes.THE_END : DimensionTypes.THE_END_PRE_1_21_9;
    }*/

    public GameMode getGameMode() {
        return gameMode;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public boolean isUseBrandName() {
        return true;
    }

    public boolean isUseJoinMessage() {
        return false;
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

    public Title getLoginTitle() {
        return loginTitle;
    }

    public Title getRegisterTitle() {
        return registerTitle;
    }

    public Title getTermsTitle() {
        return termsTitle;
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
