package alix.common.utils.multiengine.server;

import org.bukkit.Bukkit;

public final class BukkitServer implements AbstractServer {

    @Override
    public int getPort() {
        return Bukkit.getPort();
    }
}