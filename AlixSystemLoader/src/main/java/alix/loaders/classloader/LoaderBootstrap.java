package alix.loaders.classloader;

public interface LoaderBootstrap extends AbstractLoaderBootstrap {

    void onLoad();

    void onEnable();

    void onDisable();
}