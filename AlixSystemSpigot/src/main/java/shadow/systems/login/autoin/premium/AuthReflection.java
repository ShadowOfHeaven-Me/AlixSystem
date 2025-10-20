package shadow.systems.login.autoin.premium;

import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import com.google.common.collect.MapMaker;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import lombok.SneakyThrows;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.ReflectionUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static io.github.retrooper.packetevents.util.SpigotReflectionUtil.NETWORK_MANAGER_CLASS;

final class AuthReflection {

    private static final Class<?> ENCRYPTION_CLASS;
    private static final Field CHANNEL_FIELD;
    private static final NetworkManagerList managers;

    private static final Field SPOOFED_UUID_FIELD;

    static final Method encryptMethod, cipherMethod;

    static {
        managers = replaceNetworkManagerList();

        ENCRYPTION_CLASS = ReflectionUtils.nms2("util.MinecraftEncryption", "util.Crypt");

        Method encryptMethod0;
        Method cipherMethod0 = null;
        Class<?> networkManagerClass = NETWORK_MANAGER_CLASS;
        //SpigotReflectionUtil.getNetworkManagers().get(0).getClass();

        // Try to get the old (pre MC 1.16.4) encryption method
        encryptMethod0 = getMethodFirstByNameLaterByParams(networkManagerClass, "setupEncryption", SecretKey.class);

        //1.21.4
        if (encryptMethod0 == null)
            encryptMethod0 = getMethodFirstByNameLaterByParams(networkManagerClass, "setEncryptionKey", SecretKey.class);

        if (encryptMethod0 == null) {
            // Get the new encryption method
            encryptMethod0 = getMethodFirstByNameLaterByParams(networkManagerClass, "setEncryptionKey", Cipher.class, Cipher.class);

            // Get the needed Cipher helper method (used to generate ciphers from login key)
            cipherMethod0 = getMethodFirstByNameLaterByParams(ENCRYPTION_CLASS, "a", int.class, Key.class);
        }

        if (encryptMethod0 == null)
            Main.logError("Could not find the encryption method! Send this to shadow: " + ReflectionUtils.methodsToString(networkManagerClass));

        encryptMethod = encryptMethod0;
        cipherMethod = cipherMethod0;
        CHANNEL_FIELD = getChannelFieldInNetworkManagerClazz();

        SPOOFED_UUID_FIELD = getFieldByNameOrType(networkManagerClass, "spoofedUUID", UUID.class);

        if (SPOOFED_UUID_FIELD == null)
            Main.logError("Could not find the spoofedUUID field! Send this to shadow: " + AlixUtils.getFields(null, networkManagerClass));
    }

    @SneakyThrows
    private static <T> NetworkManagerList<T> replaceNetworkManagerList() {
        var managers = SpigotReflectionUtil.getNetworkManagers();
        var serverConnection = SpigotReflectionUtil.getMinecraftServerConnectionInstance();

        for (Field f : serverConnection.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            var obj = f.get(serverConnection);
            if (obj == managers) {//maybe an .equals(...)? In case some other plugin also replaces it in the meantime
                var replaced = new NetworkManagerList<>((List<T>) obj);
                f.set(serverConnection, replaced);
                return replaced;
            }
        }
        AlixUtils.debug(serverConnection.getClass().getDeclaredFields());
        throw new AlixError();
    }

    private static Field getFieldByNameOrType(Class<?> clazz, String name, Class<?> type) {
        Field f = Reflection.getField(clazz, name);
        if (f != null) return f;

        return Reflection.getField(clazz, type, 0);
    }

    private static Method getMethodFirstByNameLaterByParams(Class<?> clazz, String name, Class<?>... params) {
        Method method = Reflection.getMethod(clazz, name, params);
        if (method != null) return method;

        for (Method m : clazz.getDeclaredMethods()) {
            m.setAccessible(true);
            if (Arrays.equals(m.getParameterTypes(), params))
                return m;
        }
        for (Method m : clazz.getMethods()) {
            m.setAccessible(true);
            if (Arrays.equals(m.getParameterTypes(), params))
                return m;
        }
        return null;
    }

    @SneakyThrows
    static void setUUID(Object networkManager, UUID uuid) {
        SPOOFED_UUID_FIELD.set(networkManager, uuid);
    }

    //private static final Consumer<Object> NO_CALLBACK = AlixCommonUtils.EMPTY_CONSUMER;
    private static final Map<ChannelPromise, Consumer<Object>> MAP = new MapMaker().weakKeys().makeMap();

    static void onAdd0(Object networkManager) {
        Channel managerChannel = getChannel(networkManager);
        if (managerChannel == null || !managerChannel.isOpen()) return;

        //should be alright to sync here
        synchronized (managerChannel.voidPromise()) {
            var callback = MAP.remove(managerChannel.voidPromise());
            if (callback == null) return;

            callback.accept(networkManager);
        }
    }

    static void findNetworkManager(Channel channel, Consumer<Object> callback) {
        //Try to find it immediately
        for (Object manager : managers) {
            Channel managerChannel = getChannel(manager);
            if (managerChannel == null || !managerChannel.isOpen()) continue;

            //some plugins could've replaced the Channel field, but it's likely
            //that they hadn't replaced the channel's voidPromise() method - use identity comparison on that
            //do not use Channel#id cuz ProtocolLib is AIDS
            if (managerChannel.voidPromise() == channel.voidPromise()) {
                callback.accept(manager);
                return;
            }
        }
        //Otherwise add a callback
        //add a sync block to prevent race conditions
        synchronized (channel.voidPromise()) {
            MAP.put(channel.voidPromise(), callback);
        }
    }

    /*static Object findNetworkManager(Channel channel) {
     *//*Main.debug("CHANNEL: " + channel);
        for (Object manager : managers) {
            Main.debug("MANAGER CHANNEL: " + getChannel(manager));
        }*//*
        for (Object manager : managers) {
            Channel managerChannel = getChannel(manager);
            if (managerChannel == null || !managerChannel.isOpen()) continue;

            //some plugins could've replaced the Channel field, but it's likely
            //that they hadn't replaced the channel's id() method - use identity comparison on that
            if (managerChannel.id() == channel.id()) {
                //Main.debug("CHANNEL: " + channel);
                return manager;
            }
            //managerChannel.remoteAddress().equals((channel).remoteAddress())
        }
        AlixUtils.debug(managers.toArray());
        throw new AlixError("Could not find NetworkManager!");
        //return null;
    }*/

    private static Field getChannelFieldInNetworkManagerClazz() {
        Class<?> clazz = NETWORK_MANAGER_CLASS;
        for (Field f : clazz.getDeclaredFields()) {
            if (Channel.class.isAssignableFrom(f.getType())) {
                return f;
            }
        }
        AlixUtils.debug(clazz.getDeclaredFields());
        throw new AlixError("No Channel field found in '" + NETWORK_MANAGER_CLASS + "'!");
    }

    @SneakyThrows
    private static Channel getChannel(Object networkManager) {
        return (Channel) CHANNEL_FIELD.get(networkManager);
    }

    /*private static Channel getChannel(Object networkManager) {
        ReflectionObject wrapper = new ReflectionObject(networkManager, NETWORK_MANAGER_CLASS);
        return (Channel) wrapper.readObject(0, CHANNEL_CLASS);
    }*/
}