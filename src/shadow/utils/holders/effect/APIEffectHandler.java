package shadow.utils.holders.effect;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import shadow.Main;
import shadow.utils.users.Verifications;
import shadow.utils.users.types.UnverifiedUser;

import java.util.Collection;

final class APIEffectHandler extends AbstractEffectHandler {

    private final Player player;
    private Collection<PotionEffect> potionEffects;
    private boolean removing;

    APIEffectHandler(UnverifiedUser user) {
        this.player = user.getPlayer();
    }

    @Override
    public void resetEffects() {
        this.removing = true;
        this.potionEffects = this.player.getActivePotionEffects();
        this.potionEffects.forEach(e -> this.player.removePotionEffect(e.getType()));
        this.removing = false;
    }

    @Override
    public void returnEffects() {

    }

    static {
        Main.pm.registerEvents(new EffectListener(), Main.plugin);
    }

    private static final class EffectListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPotionEffect(EntityPotionEffectEvent event) {
            if (event.isCancelled()) return;
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                UnverifiedUser user = Verifications.get(player);

                if (user == null) return;

                APIEffectHandler handler = (APIEffectHandler) user.potionEffectHandler;

                if (handler.removing) return;

                switch (event.getAction()) {
                    case CLEARED:
                }
            }
        }
    }
}