package alix.api.user.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.UUID;

public interface AlixUserData {

    default boolean isPremium() {
        return this.getPremiumStatus().isPremium();
    }

    boolean matchesPassword(@NotNull String str);

    boolean hasSecondaryPassword();

    boolean hasSecondaryPasswordActive();

    boolean matchesSecondaryPassword(@NotNull String str);

    @NotNull
    PremiumStatus getPremiumStatus();

    @Nullable
    UUID premiumUUID();

    @NotNull
    InetAddress getSavedIP();

    @NotNull
    String getName();

    long getLastSuccessfulLogin();
}