package alix.velocity.systems.events;

import alix.velocity.systems.autoin.PremiumAutoIn;
import com.github.games647.fastlogin.velocity.event.VelocityFastLoginPreLoginEvent;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;

public final class FastLoginEvents {

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(VelocityFastLoginPreLoginEvent event) {
        if (event.getProfile().isPremium()) PremiumAutoIn.add(event.getUsername());
        else PremiumAutoIn.remove(event.getUsername());
    }
}