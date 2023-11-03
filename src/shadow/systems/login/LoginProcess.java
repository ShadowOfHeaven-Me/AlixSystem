/*
package shadow.systems.login;

import alix.common.scheduler.JavaScheduler;
import alix.common.scheduler.tasks.SchedulerTask;
import shadow.utils.main.JavaUtils;
import shadow.utils.objects.filter.packet.types.PacketBlocker;
import shadow.utils.users.offline.TeleportUser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public class LoginProcess implements Runnable {

    final static PotionEffect spawnProtectionEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 255, false, false, false);
    final Player p;
    final Location primordialLocation, teleportLocation;
    final SchedulerTask task;
    final String message;
    short i;

    public LoginProcess(TeleportUser user) {
        p = user.getPlayer();
        primordialLocation = p.getLocation();
        teleportLocation = primordialLocation.clone();
        teleportLocation.setY(10000);
        p.teleport(teleportLocation);
        task = JavaScheduler.repeatSync(this, 3L, TimeUnit.SECONDS);
        //task = Bukkit.getScheduler().runTaskTimer(Main.instance, this, 60L, 60L);
        message = JavaUtils.getVerificationReminderMessage(user.isRegistered());
    }

    public void end() {
        task.cancel();
        p.addPotionEffect(spawnProtectionEffect);
        p.teleport(primordialLocation);
    }

    private void teleport() {
        p.teleport(teleportLocation);
    }

*/
/*    private boolean check() {
        if (++i == JavaUtils.packetsBeforeNotLoggedInKick) {
            task.cancel();
            p.kickPlayer(PacketBlocker.loginTimePassed);
            return true;
        }
        return false;
    }*//*


    @Override
    public void run() {
        //if (check()) return;
        teleport();
        p.sendMessage(message);
    }
}*/
