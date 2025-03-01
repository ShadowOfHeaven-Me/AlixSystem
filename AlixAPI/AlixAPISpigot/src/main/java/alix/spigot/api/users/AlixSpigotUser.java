package alix.spigot.api.users;

import org.bukkit.entity.Player;

/**
 * An Alix user that has already joined (has a {@link Player} instance)
 **/
public interface AlixSpigotUser extends AbstractSpigotUser {

    Player getPlayer();

}