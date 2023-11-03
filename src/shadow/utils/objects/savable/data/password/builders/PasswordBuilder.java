package shadow.utils.objects.savable.data.password.builders;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface PasswordBuilder {

    /**
     * Throws an exception if the used gui cannot be returned
     *
     * @return The Inventory gui used for password building
     *
     * @author ShadowOfHeaven
     */

    @NotNull
    default Inventory getGUI() {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns whether the player should be registered/logged in after the slot select
     *
     * @param slot The inventory slot clicked
     *
     * @return Whether the Player should have his password (returned with the {@code this.getPasswordBuilt()})
     * used for an attempted verification (either register or login)
     *
     * @author ShadowOfHeaven
     */

    default boolean select(int slot) {
        throw new UnsupportedOperationException();
    }

/*    *//**
     * Returns whether the player should be registered/logged in after the String input
     *
     * @param text The String inputted
     *
     * @author ShadowOfHeaven
     *//*

    default void input(String text, boolean valid) {
        throw new UnsupportedOperationException();
    }*/

    /**
     * Returns the builder type
     *
     * @return The password builder type
     *
     * @author ShadowOfHeaven
     */

    BuilderType getType();


    /**
     * @return The currently created password with the builder
     *
     * @author ShadowOfHeaven
     */

    @NotNull
    String getPasswordBuilt();

}