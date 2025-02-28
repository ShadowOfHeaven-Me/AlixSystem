package alix.common.utils.config;

import alix.common.utils.AlixCommonHandler;

public interface ConfigProvider {

    int getInt(String s);

    int getInt(String s, int def);

    boolean getBoolean(String s);

    boolean getBoolean(String s, boolean def);

    String getString(String s);

    ConfigProvider config = AlixCommonHandler.createConfigProviderImpl();

}