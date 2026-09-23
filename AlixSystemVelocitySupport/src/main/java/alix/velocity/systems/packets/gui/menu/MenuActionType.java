package alix.velocity.systems.packets.gui.menu;

/**
 * The kinds of things a configured menu item's click action ("[type] value" in a gui-menus/*.yml file)
 * can do. Items with genuinely dynamic, stateful behavior (a live setting, a cycling value, an input
 * flow) don't use these - they're marked with an "internal" id instead (see MenuItemDef) so their
 * whole appearance and behavior stays owned by the specific menu's Java code.
 */
public enum MenuActionType {

    MESSAGE,
    PLAYER_COMMAND,
    CONSOLE_COMMAND,
    SOUND,
    OPEN_MENU,
    CLOSE,
    //Explicit no-op, for purely decorative items.
    NONE
}
