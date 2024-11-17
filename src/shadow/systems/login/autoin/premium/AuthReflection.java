package shadow.systems.login.autoin.premium;

import alix.libs.com.github.retrooper.packetevents.util.reflection.Reflection;
import alix.libs.com.github.retrooper.packetevents.util.reflection.ReflectionObject;
import alix.libs.io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.Channel;
import shadow.utils.misc.ReflectionUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.security.Key;
import java.util.List;

import static alix.libs.io.github.retrooper.packetevents.util.SpigotReflectionUtil.CHANNEL_CLASS;
import static alix.libs.io.github.retrooper.packetevents.util.SpigotReflectionUtil.NETWORK_MANAGER_CLASS;

final class AuthReflection {

    private static final Class<?> ENCRYPTION_CLASS;
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
    }

    //todo: uh, by ip?
    static Object findNetworkManager(Object channel) {
        List<Object> managers = SpigotReflectionUtil.getNetworkManagers();
        for (Object manager : managers) {
            Channel managerChannel = getChannel(manager);
            if (managerChannel.remoteAddress().equals(((Channel) channel).remoteAddress())) {
                return manager;
            }
        }
        return null;
    }

    private static Channel getChannel(Object networkManager) {
        ReflectionObject wrapper = new ReflectionObject(networkManager, NETWORK_MANAGER_CLASS);
        return (Channel) wrapper.readObject(0, CHANNEL_CLASS);
    }
}