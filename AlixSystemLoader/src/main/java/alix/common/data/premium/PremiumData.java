package alix.common.data.premium;

import ua.nanit.limbo.util.UUIDUtil;

import java.util.UUID;

public interface PremiumData {

    UUID premiumUUID();

    PremiumStatus getStatus();

    String toSavable();

    static PremiumData fromSavable(String line) {
        switch (line) {
            case "0"://no data
                return UNKNOWN;
            case "-1"://non-premium
                return NON_PREMIUM;
            default://premium
                return new PremiumDataImpl(UUID.fromString(line));
        }
    }

    static PremiumData createNew(UUID premiumUUID) {
        return new PremiumDataImpl(premiumUUID);
    }

    static PremiumData createNew(String premiumUUID) {
        return new PremiumDataImpl(UUIDUtil.fromString(premiumUUID));
    }

    PremiumData NON_PREMIUM = new NonPremiumImpl();
    PremiumData UNKNOWN = new UnknownPremiumImpl();
}