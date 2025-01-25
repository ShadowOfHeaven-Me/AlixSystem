package alix.common.data.premium;

import java.util.UUID;

final class NonPremiumImpl implements PremiumData {

    NonPremiumImpl() {
    }

    @Override
    public UUID premiumUUID() {
        return null;
    }

    @Override
    public PremiumStatus getStatus() {
        return PremiumStatus.NON_PREMIUM;
    }

    @Override
    public String toSavable() {
        return "-1";
    }
}