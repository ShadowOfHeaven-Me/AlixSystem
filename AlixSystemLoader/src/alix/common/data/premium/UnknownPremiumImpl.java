package alix.common.data.premium;

import java.util.UUID;

final class UnknownPremiumImpl implements PremiumData {

    @Override
    public String toSavable() {
        return "0";
    }

    @Override
    public UUID premiumUUID() {
        return null;
    }

    @Override
    public PremiumStatus getStatus() {
        return PremiumStatus.UNKNOWN;
    }
}