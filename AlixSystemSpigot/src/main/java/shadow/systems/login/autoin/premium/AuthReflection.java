package shadow.systems.login.autoin.premium;

import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import shadow.utils.main.AlixUtils;
import shadow.utils.misc.ReflectionUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.List;

import static io.github.retrooper.packetevents.util.SpigotReflectionUtil.NETWORK_MANAGER_CLASS;

final class AuthReflection {

    private static final Class<?> ENCRYPTION_CLASS;
    private static final Field CHANNEL_FIELD;
    private static final List<Object> managers = SpigotReflectionUtil.getNetworkManagers();
    static final Method encryptMethod, cipherMethod;

    static {
        ENCRYPTION_CLASS = ReflectionUtils.nms2("util.MinecraftEncryption", "util.Crypt");

        Method encryptMethod0 = null;
        Method cipherMethod0 = null;
        if (encryptMethod0 == null) {
            Class<?> networkManagerClass = SpigotReflectionUtil.getNetworkManagers().get(0).getClass();

            // Try to get the old (pre MC 1.16.4) encryption method
            encryptMethod0 = Reflection.getMethod(networkManagerClass, "setupEncryption", SecretKey.class);

            if (encryptMethod0 == null) {
                // Get the new encryption method
                encryptMethod0 = Reflection.getMethod(networkManagerClass, "setEncryptionKey", Cipher.class, Cipher.class);

                // Get the needed Cipher helper method (used to generate ciphers from login key)
                cipherMethod0 = Reflection.getMethod(ENCRYPTION_CLASS, "a", int.class, Key.class);
            }
        }
        encryptMethod = encryptMethod0;
        cipherMethod = cipherMethod0;
        CHANNEL_FIELD = getChannelFieldInNetworkManagerClazz();
    }

    static Object findNetworkManager(Channel channel) {
        /*Main.debug("CHANNEL: " + channel);
        for (Object manager : managers) {
            Main.debug("MANAGER CHANNEL: " + getChannel(manager));
        }*/
        for (Object manager : managers) {
            Channel managerChannel = getChannel(manager);
            if (managerChannel == null || !managerChannel.isOpen()) continue;

            //some plugins could've replaced the Channel field, but it's likely
            //that they hadn't replaced the channel's pipeline() method - use identity comparison on that
            //if (managerChannel.pipeline() == channel.pipeline()) return manager;
            if (managerChannel.pipeline() == channel.pipeline()) {
                //Main.debug("CHANNEL: " + channel);
                return manager;
            }
            //managerChannel.remoteAddress().equals((channel).remoteAddress())
        }
        AlixUtils.debug(managers.toArray());
        throw new AlixError("Could not find NetworkManager!");
        //return null;
    }

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