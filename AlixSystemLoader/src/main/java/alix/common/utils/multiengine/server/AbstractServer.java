package alix.common.utils.multiengine.server;

import alix.common.utils.AlixCommonHandler;

public interface AbstractServer {

    int getPort();

    AbstractServer INSTANCE = AlixCommonHandler.createServerAccessorImpl();
}