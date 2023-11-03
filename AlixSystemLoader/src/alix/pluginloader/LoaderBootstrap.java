package alix.pluginloader;

public interface LoaderBootstrap {

    void onLoad();

    void onEnable();

    void onDisable();
}