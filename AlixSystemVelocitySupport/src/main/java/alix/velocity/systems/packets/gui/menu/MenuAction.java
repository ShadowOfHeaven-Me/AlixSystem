package alix.velocity.systems.packets.gui.menu;

import alix.common.AlixCommonMain;
import alix.common.utils.formatter.AlixFormatter;
import alix.velocity.Main;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import ua.nanit.limbo.connection.login.packets.SoundPackets;

/**
 * A single configured click action, parsed from a line like "[message] &aHello!" (a bracket-prefixed
 * type followed by its value). See MenuActionType for the supported types.
 */
public final class MenuAction {

    private final MenuActionType type;
    private final String value;

    private MenuAction(MenuActionType type, String value) {
        this.type = type;
        this.value = value;
    }

    public static MenuAction parse(String raw) {
        if (raw == null) return null;
        String line = raw.trim();
        if (line.isEmpty()) return null;

        if (!line.startsWith("[")) {
            AlixCommonMain.logWarning("Invalid menu action '" + raw + "' - expected a format like \"[message] some text\"");
            return null;
        }
        int end = line.indexOf(']');
        if (end < 0) {
            AlixCommonMain.logWarning("Invalid menu action '" + raw + "' - missing closing ']'");
            return null;
        }

        String typeName = line.substring(1, end).trim().toUpperCase().replace('-', '_').replace(' ', '_');
        String value = end + 1 < line.length() ? line.substring(end + 1).trim() : "";

        MenuActionType type;
        try {
            type = MenuActionType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            AlixCommonMain.logWarning("Unknown menu action type '" + typeName + "' in '" + raw + "'");
            return null;
        }
        return new MenuAction(type, value);
    }

    public MenuActionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void run(VerifiedUser user) {
        switch (this.type) {
            case MESSAGE:
                user.sendMessage(AlixFormatter.translateColors(this.value));
                return;
            case PLAYER_COMMAND: {
                String cmd = stripLeadingSlash(this.value);
                Main.SERVER.getCommandManager().executeImmediatelyAsync(user.getPlayer(), cmd);
                return;
            }
            case CONSOLE_COMMAND: {
                String cmd = stripLeadingSlash(this.value);
                Main.SERVER.getCommandManager().executeImmediatelyAsync(Main.SERVER.getConsoleCommandSource(), cmd);
                return;
            }
            case SOUND: {
                Sound sound = Sounds.getByName(this.value);
                if (sound == null) {
                    AlixCommonMain.logWarning("Menu action \"[sound] " + this.value + "\" references an unknown sound");
                    return;
                }
                user.writePacketSilently(SoundPackets.wrapperOf(sound));
                return;
            }
            case OPEN_MENU:
                MenuRegistry.open(this.value, user);
                return;
            case CLOSE:
                user.closeInventory();
                return;
            case NONE:
                return;
        }
    }

    private static String stripLeadingSlash(String cmd) {
        return cmd.startsWith("/") ? cmd.substring(1) : cmd;
    }
}
