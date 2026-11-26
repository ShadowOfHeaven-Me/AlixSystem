package alix.common.utils.multiengine.server;

import alix.common.utils.AlixCommonHandler;

public interface AbstractServer<C> {

    void sendMessage(C receiver, String message);

    AbstractServer<?> INSTANCE = AlixCommonHandler.createServerAccessorImpl();
}