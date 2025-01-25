/*
package shadow.systems.bungee;

import alix.common.data.security.password.Password;
import alix.common.utils.other.throwable.AlixException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AlixBungee {

    private static final Map<UUID, Password> map = new ConcurrentHashMap<>();
    private static final String ID_TAG = "alix:ver";
    public static final boolean isEnabled = SpigotConfig.bungee;

    public static void init() {
        if (!isEnabled) return;
        */
/*Bukkit.getMessenger().registerIncomingPluginChannel(Main.plugin, ID_TAG, new Interpreter());
        Bukkit.getMessenger().registerOutgoingPluginChannel(Main.plugin, ID_TAG);*//*

    }

    private static final class Interpreter implements PluginMessageListener {

        @Override
        public void onPluginMessageReceived(@NotNull String tag, @NotNull Player player, @NotNull byte[] bytes) {
            if (!tag.equals(ID_TAG)) return;

            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
            Intention intention = Intention.values()[input.readByte()];

            if (intention == Intention.INFORM_BACKEND_DATA) {
                Password password = Password.read(input);
            }
            throw new AlixException("Intention from Bungee " + intention.name() + " is not readable!");
        }
    }
}*/
