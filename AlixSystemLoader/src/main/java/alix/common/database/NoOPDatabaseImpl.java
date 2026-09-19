package alix.common.database;

import alix.common.data.AuthSetting;
import alix.common.data.Identity;
import alix.common.data.LoginType;
import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseType;
import alix.common.utils.other.keys.secret.MapSecretKey;
import alix.common.utils.other.throwable.AlixError;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class NoOPDatabaseImpl implements DatabaseUpdater {
    @Override
    public CompletableFuture<Void> loadAllUsers(Map<String, PersistentUserData> map) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> loadAllTokens(Map<MapSecretKey, String> map) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void connect() {

    }

    @Override
    public void saveData(PersistentUserData data) {

    }

    @Override
    public void createTablesSync() {

    }

    @Override
    public void clearPasswordPointers(String name) {

    }

    @Override
    public void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin) {

    }

    @Override
    public void updateIpByName(String name, String ip) {

    }

    @Override
    public void updateFingerprintByName(String name, int fingerprint) {

    }

    @Override
    public void updatePasswordByOwner(String ownerName, Password password) {

    }

    @Override
    public void setPremiumData(String name, PremiumData data) {

    }

    @Override
    public void setPassword(String name, Password newPass, boolean isMain) {

    }

    @Override
    public void saveUserToken(Identity identity, String token) {

    }

    @Override
    public void commitTokenAndEmail(Identity identity, String token, String name, String savableEmail) {

    }

    @Override
    public void saveRecoveryCodes(Identity identity, String joinedCodes) {

    }

    @Override
    public void loadRecoveryCodes(Identity identity, Consumer<String> consumer) {
        consumer.accept(null);
    }

    @Override
    public void tryConsumeRecoveryCode(Identity identity, String typedCode, Consumer<Boolean> callback) {
        callback.accept(false);
    }

    @Override
    public void loadUser(String name, Consumer<PersistentUserData> consumer) {
        throw new AlixError("loadUser called on NOOP db impl");
    }

    @Override
    public void updateAuthSettingsByName(String name, AuthSetting authSettings) {

    }

    @Override
    public void updateHasProvenAuthAccessByName(String name, boolean hasProvenAuthAccess) {

    }

    @Override
    public void updateIpAutoLoginByName(String name, Boolean ipAutoLogin) {

    }

    @Override
    public void updateLoginTypeByName(String name, LoginType loginType) {

    }

    @Override
    public void updateExtraLoginTypeByName(String name, LoginType extraLoginType) {

    }

    @Override
    public void updateEmailByName(String name, String email) {

    }

    @Override
    public void removeByName(String name) {

    }

    @Override
    public DatabaseType getType() {
        return null;
    }
}