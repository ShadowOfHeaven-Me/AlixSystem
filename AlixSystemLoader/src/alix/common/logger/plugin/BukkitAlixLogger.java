package alix.common.logger.plugin;

import com.github.retrooper.packetevents.util.ColorUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class BukkitAlixLogger extends Logger {

    //private final String name = AlixLoggerProvider.LOGGER_NAME_FULL;
    private final String prefixText;

    public BukkitAlixLogger() {
        super("Sex",null);
        this.prefixText = ColorUtil.toString(NamedTextColor.RED) + "[AlixSystem] " + ColorUtil.toString(NamedTextColor.GRAY);
    }

    @Override
    public void log(LogRecord record) {
        Bukkit.getConsoleSender().sendMessage(this.prefixText + record.getMessage());
    }

    /*private String color(Level level) {

    }*/
}