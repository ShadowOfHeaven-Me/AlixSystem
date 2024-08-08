package alix.loaders.classloader;

import alix.common.utils.other.throwable.AlixException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class JarInJarClassLoader<E extends AbstractLoaderBootstrap> extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * Creates a new jar-in-jar class loader.
     *
     * @param loaderClassLoader the loader plugin's classloader (setup and created by the platform)
     * @param jarResourcePath   the path to the jar-in-jar resource within the loader jar
     * @throws AlixLoadingException if something unexpectedly bad happens
     */
    public JarInJarClassLoader(ClassLoader loaderClassLoader, String jarResourcePath) throws AlixLoadingException {
        super(new URL[]{extractJar(loaderClassLoader, jarResourcePath)}, loaderClassLoader);
    }

/*    public void addJarToClasspath(URL url) {
        addURL(url);
    }

    public void deleteJarResource() {
        URL[] urls = getURLs();
        if (urls.length == 0) {
            return;
        }

        try {
            Path path = Paths.get(urls[0].toURI());
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // ignore
        }
    }*/

    public <T> T instantiateClass(String bootstrapClass, Class<T> subclass) throws AlixLoadingException {
        Class<T> clazz;
        try {
            clazz = (Class<T>) loadClass(bootstrapClass).asSubclass(subclass);
        } catch (ReflectiveOperationException e) {
            throw new AlixLoadingException("Unable to load bootstrap class", e);
        }

        try {
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AlixLoadingException("Unable to create bootstrap plugin instance", e);
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            throw new AlixException(e);
        }
    }

    /**
     * Creates a new plugin instance.
     *
     * @param bootstrapClass   the name of the bootstrap plugin class
     * @param loaderPluginType the type of the loader plugin, the only parameter of the bootstrap plugin constructor
     * @param loaderPlugin     the loader plugin instance
     * @param <T>              the type of the loader plugin
     * @return the instantiated bootstrap plugin
     */
    public <T> AbstractLoaderBootstrap instantiatePlugin(String bootstrapClass, Class<T> loaderPluginType, T loaderPlugin) throws AlixLoadingException {
        Class<? extends AbstractLoaderBootstrap> plugin;
        try {
            plugin = loadClass(bootstrapClass).asSubclass(AbstractLoaderBootstrap.class);
        } catch (ReflectiveOperationException e) {
            throw new AlixLoadingException("Unable to load bootstrap class", e);
        }

        Constructor<? extends AbstractLoaderBootstrap> constructor;
        try {
            constructor = plugin.getConstructor(loaderPluginType);
        } catch (ReflectiveOperationException e) {
            throw new AlixLoadingException("Unable to get bootstrap constructor", e);
        }

        try {
            return constructor.newInstance(loaderPlugin);
        } catch (ReflectiveOperationException e) {
            throw new AlixLoadingException("Unable to create bootstrap plugin instance", e);
        }
    }

    /**
     * Extracts the "jar-in-jar" from the loader plugin into a temporary file,
     * then returns a URL that can be used by the {@link JarInJarClassLoader}.
     *
     * @param loaderClassLoader the classloader for the "host" loader plugin
     * @param jarResourcePath   the inner jar resource path
     * @return a URL to the extracted file
     */
    private static URL extractJar(ClassLoader loaderClassLoader, String jarResourcePath) throws AlixLoadingException {
        // get the jar-in-jar resource
        URL jarInJar = loaderClassLoader.getResource(jarResourcePath);
        if (jarInJar == null) {
            throw new AlixLoadingException("Could not locate jar-in-jar");
        }

        // create a temporary file
        // on posix systems by default this is only read/writable by the process owner
        Path path;
        try {
            path = Files.createTempFile("alixsystem", ".jar.tmp");
        } catch (IOException e) {
            throw new AlixLoadingException("Unable to create a temporary file", e);
        }

        // mark that the file should be deleted on exit
        path.toFile().deleteOnExit();

        // copy the jar-in-jar to the temporary file path
        try (InputStream in = jarInJar.openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AlixLoadingException("Unable to copy jar-in-jar to temporary path", e);
        }

        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new AlixLoadingException("Unable to get URL from path", e);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}