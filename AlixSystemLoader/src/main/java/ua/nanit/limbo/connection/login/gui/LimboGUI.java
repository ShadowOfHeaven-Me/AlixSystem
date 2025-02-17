package ua.nanit.limbo.connection.login.gui;

public interface LimboGUI {

    void select(int slot);

    void onCloseAttempt();

    void show();

    default boolean isAnvil() {
        return this instanceof LimboAnvilBuilder;
    }
}