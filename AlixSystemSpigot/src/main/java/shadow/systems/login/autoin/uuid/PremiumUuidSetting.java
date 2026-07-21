package shadow.systems.login.autoin.uuid;

import alix.common.AlixCommonMain;
import alix.common.environment.ServerEnvironment;
import org.bukkit.Bukkit;
import shadow.Main;

import java.util.UUID;

public enum PremiumUuidSetting {

    TRUE,
    AUTO,
    FALSE;

    public boolean shouldAssignPremiumUuid(String name, UUID premiumUUID) {
        return switch (CONFIG) {
            case TRUE -> true;
            case FALSE -> false;
            case AUTO -> {
                //todo: account for lower paper?
                boolean paper = ServerEnvironment.isPaper();
                var cached = paper ? Bukkit.getOfflinePlayerIfCached(name) : Bukkit.getOfflinePlayer(name);
                //give him premium uuid if he wasn't seen before or was already known under this premium uuid
                yield cached == null || (paper || cached.hasPlayedBefore()) && cached.getUniqueId().equals(premiumUUID);
            }
        };
    }

    public static final PremiumUuidSetting CONFIG = config();

    private static PremiumUuidSetting config() {
        var cfg = Main.config.getString("premium-uuid");
        try {
            return valueOf(cfg.toUpperCase());
        } catch (IllegalArgumentException e) {
            AlixCommonMain.logWarning("Invalid config parameter 'premium-uuid', got: '" + cfg + "', for \"true, auto, false\" being the only valid options!");
            return AUTO;//config default
        }
    }
}