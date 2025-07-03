package alix.common.utils.multiengine.ban;

import alix.common.utils.AlixCommonHandler;

import java.util.Date;

public interface AbstractBanList {

    void ban(String target, String reason, Date until, String sender);

    void unban(String target);

    boolean isBanned(String target);

    AbstractBanList IP = AlixCommonHandler.createBanListImpl(true);
    AbstractBanList NAME = AlixCommonHandler.createBanListImpl(false);
}