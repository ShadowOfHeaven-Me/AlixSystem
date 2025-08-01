package alix.velocity.systems.events.premium;

import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.login.premium.EncryptionData;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.proxy.connection.MinecraftConnection;

public record EncryptionInfo(EncryptionData encryptionData, Continuation continuation, MinecraftConnection minecraftConnection, PremiumData premiumData, PersistentUserData data) {
}