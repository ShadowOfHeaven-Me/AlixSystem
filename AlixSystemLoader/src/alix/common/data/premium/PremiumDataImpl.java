package alix.common.data.premium;

import java.util.UUID;

final class PremiumDataImpl implements PremiumData {

    //private final String name;
    private final UUID uuid;

    PremiumDataImpl(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID premiumUUID() {
        return this.uuid;
    }

    @Override
    public PremiumStatus getStatus() {
        return PremiumStatus.PREMIUM;
    }

    @Override
    public String toSavable() {
        return this.uuid.toString();
    }
}