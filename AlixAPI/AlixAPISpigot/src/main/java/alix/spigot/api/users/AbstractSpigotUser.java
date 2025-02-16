package alix.spigot.api.users;

import alix.api.user.AlixCommonUser;
import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;

/**
 * An Alix user that is either still connecting or has already joined.
 * See {@link AbstractSpigotUser#hasJoined()}
 **/
public interface AbstractSpigotUser extends AlixCommonUser {

    @NotNull
    User retrooperUser();

    @NotNull
    default String getName() {
        return this.retrooperUser().getName();
    }

    /**
     * Defines whether this user instance is one that has fully joined the server
     **/
    default boolean hasJoined() {
        return this instanceof AlixSpigotUser;
    }

    /**
     * Defines whether this user instance is one that hasn't fully joined the server,
     * and is still in the connecting phase
     **/
    default boolean isJoining() {
        return !this.hasJoined();
    }
}