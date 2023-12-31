package alix.common.utils.config;

import alix.common.utils.AlixCommonHandler;

public interface ConfigProvider {

    int getInt(String s);

    boolean getBoolean(String s);

    String getString(String s);


    ConfigProvider config = AlixCommonHandler.createConfigProviderImpl();
}