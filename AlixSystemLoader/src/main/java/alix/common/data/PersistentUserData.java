package alix.common.data;

import alix.api.user.data.AlixUserData;
import alix.api.user.data.PremiumStatus;
import alix.common.AlixCommonMain;
import alix.common.antibot.ip.IPUtils;
import alix.common.connection.filters.GeoIPTracker;
import alix.common.data.file.AllowListFileManager;
import alix.common.data.file.UserFileManager;
import alix.common.data.loc.AlixLocationList;
import alix.common.data.loc.provider.LocationListProvider;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumIdIndex;
import alix.common.data.security.email.Email;
import alix.common.data.security.password.Password;
import alix.common.database.DatabaseUpdater;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.file.SaveUtils;
import alix.common.utils.other.keys.secret.MapSecretKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ua.nanit.limbo.util.UUIDUtil;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;

public final class PersistentUserData implements AlixUserData {

    public static final String NO_VALUE = "0";
    public static final InetAddress UNKNOWN_IP = InetAddress.getLoopbackAddress();
    private static final LocationListProvider homesProvider = LocationListProvider.IMPL;
    private static final DatabaseUpdater database = DatabaseUpdater.INSTANCE;
    private static final int CURRENT_DATA_LENGTH = 13;
    private final AlixLocationList homes;
    private final String name;
    private final UUID uuid;
    private final LoginParams loginParams;
    private final long createdAt;
    @SuppressWarnings("NotNullFieldNotInitialized")//IJ tweaking
    @NotNull
    private volatile PremiumData premiumData;
    @NotNull
    private volatile InetAddress ip;
    private volatile long mutedUntil, lastSuccessfulLogin;
    private final Identity identity;
    private volatile Email email;

    //name | password1 ; password2 | ip | homes | mutedUntil | login type1 ; login type2 | login settings | lastSuccessfulLogin | premium data
    //createdAt | email | identity
    private PersistentUserData(String[] splitData) {
        //splitData = ensureSplitDataCorrectness(splitData);
        this.name = splitData[0];
        this.identity = Identity.fromSaved(this.name, splitData[12]);//very important to load it first
        this.uuid = this._uuid();
        this.loginParams = new LoginParams(splitData[1]);
        this.ip = IPUtils.fromAddress(splitData[2]);
        this.homes = homesProvider.fromSavable(splitData[3]);
        this.mutedUntil = Long.parseLong(splitData[4]);
        this.loginParams.initLoginTypes(splitData[5]);
        this.loginParams.initSettings(splitData[6]);
        this.loginParams.initAuthSettings(splitData[7]);
        this.lastSuccessfulLogin = Long.parseLong(splitData[8]);
        this.setPremiumData0(PremiumData.fromSavable(splitData[9]));

        var createdAtStr = splitData[10];
        //if no data let's just store it from now on
        this.createdAt = createdAtStr.equals(NO_VALUE) ? System.currentTimeMillis() : Long.parseLong(createdAtStr);
        this.readEmail(splitData[11]);

        UserFileManager.putData(this);
        GeoIPTracker.addExisting(this.ip);//Add it here, as it was loaded
    }

    private PersistentUserData(String name, InetAddress ip, Password password) {
        this.name = name;
        this.uuid = this._uuid();
        this.ip = ip;
        this.loginParams = new LoginParams(password);
        this.homes = homesProvider.newList();
        this.premiumData = PremiumData.UNKNOWN;
        this.createdAt = System.currentTimeMillis();
        this.identity = Identity.newIdentity(name);
        UserFileManager.putData(this);

        GeoIPTracker.addExisting(ip);
        GeoIPTracker.removeTemporary(ip);

        this.saveToDatabase();
    }

    //during a premium name change
    private PersistentUserData(String name, PersistentUserData data) {
        this.name = name;
        this.uuid = this._uuid();
        this.ip = data.ip;
        this.createdAt = data.createdAt;
        this.premiumData = data.premiumData;
        this.homes = data.homes;
        this.loginParams = data.loginParams;
        this.email = data.email;
        this.identity = data.identity;
        UserFileManager.putData(this);

        this.saveToDatabase();
    }

    public PersistentUserData(AlixLocationList homes, String name, UUID uuid, LoginParams loginParams, long createdAt, Identity identity, Email email, long lastSuccessfulLogin, @NotNull InetAddress ip, @NotNull PremiumData premiumData, long mutedUntil) {
        this.homes = homes;
        this.name = name;
        this.uuid = uuid;
        this.loginParams = loginParams;
        this.createdAt = createdAt;
        this.identity = identity;
        this.email = email;
        this.lastSuccessfulLogin = lastSuccessfulLogin;
        this.ip = ip;
        this.premiumData = premiumData;
        this.mutedUntil = mutedUntil;
    }

    void readEmail(String data) {
        try {
            this.email = Email.readFromSaved(data, this.tokenKey());
        } catch (Exception e) {
            AlixCommonMain.logWarning("Failed to read encrypted email: " + e.getMessage() + ". Was the 'user-tokens' file tampered with or deleted?");
        }
    }

    public boolean setEmail(String email) {
        try {
            this.email = Email.fromEmail(email, this.tokenKey());
            return true;
        } catch (Exception e) {
            AlixCommonUtils.logException(e);
            return false;
        }
    }

    @Override
    public String toString() {
        return SaveUtils.asSavable('|',
                name,
                loginParams.passwordsToSavable(),
                ip.getHostAddress(),
                homes.toSavable(),
                mutedUntil,
                loginParams.loginTypesToSavable(),
                loginParams.ipAutoLoginToSavable(),
                loginParams.authSettingsToSavable(),
                lastSuccessfulLogin,
                premiumData.toSavable(),
                this.createdAt,
                this.emailSavable(),
                this.identity.identity()
        );
    }

    public MapSecretKey<UUID> tokenKey() {
        return MapSecretKey.fromName(this.identity.identity());
    }

    public void saveToDatabase() {
        database.saveData(this);
        /*database.insertUser(this.name, this.uuid, this.createdAt, this.getPassword());

        database.setPremiumData(this.name, this.premiumData);*/
    }

    private UUID _uuid() {
        return UUIDUtil.getOfflineModeUuid(this.name);
    }

    private PersistentUserData(
            String name,
            UUID uuid,
            long createdAt,
            long lastSuccessfulLogin,
            InetAddress ip,
            long mutedUntil,
            LoginType loginType,
            LoginType extraLoginType,
            @Nullable Boolean ipAutoLogin,
            AuthSetting authSettings,
            boolean hasProvenAuthAccess,
            Identity identity,
            @Nullable Email email,
            AlixLocationList homes,
            PremiumData premiumData,
            Password password,
            @Nullable Password extraPassword
    ) {
        this.name = name;
        this.uuid = uuid == null ? this._uuid() : uuid;
        this.createdAt = createdAt;
        this.lastSuccessfulLogin = lastSuccessfulLogin;
        this.ip = ip == null ? UNKNOWN_IP : ip;
        this.mutedUntil = mutedUntil;
        this.identity = identity == null ? Identity.newIdentity(name) : identity;

        this.loginParams = new LoginParams(password == null ? Password.empty() : password);
        this.loginParams.setLoginType(loginType);
        this.loginParams.setExtraLoginType(extraLoginType);
        this.loginParams.initSettings(String.valueOf(ipAutoLogin));
        this.loginParams.setAuthSettings(authSettings);
        this.loginParams.setHasProvenAuthAccess(hasProvenAuthAccess);
        this.loginParams.setExtraPassword(extraPassword);

        this.email = email;
        this.homes = homes == null ? homesProvider.newList() : homes;
        this.premiumData = premiumData == null ? PremiumData.UNKNOWN : premiumData;

        UserFileManager.putData(this);

        if (!this.ip.equals(UNKNOWN_IP)) {
            GeoIPTracker.addExisting(this.ip);
        }

        if (this.premiumData.getStatus().isPremium()) {
            PremiumIdIndex.index(this.premiumData.premiumUUID());
        }
    }

    public static PersistentUserData fromDatabase(
            String name,
            UUID uuid,
            long createdAt,
            long lastSuccessfulLogin,
            InetAddress ip,
            long mutedUntil,
            LoginType loginType,
            LoginType extraLoginType,
            Boolean ipAutoLogin,
            AuthSetting authSettings,
            boolean hasProvenAuthAccess,
            Identity identity,
            Email email,
            AlixLocationList homes,
            PremiumData premiumData,
            Password password,
            Password extraPassword
    ) {
        return new PersistentUserData(
                name,
                uuid,
                createdAt,
                lastSuccessfulLogin,
                ip,
                mutedUntil,
                loginType,
                extraLoginType,
                ipAutoLogin,
                authSettings,
                hasProvenAuthAccess,
                identity,
                email,
                homes,
                premiumData,
                password,
                extraPassword
        );
    }

    public static boolean registerPremiumPlayerRename(String newName, PremiumData premiumData) {
        var renamedPlayersData = UserFileManager.getAllData().stream()
                .filter(data -> premiumData.premiumUUID().equals(data.getPremiumData().premiumUUID())).findAny().orElse(null);

        if (renamedPlayersData == null)
            return false;

        AlixCommonMain.logInfo("Detected premium player's " + newName + " (Formerly " + renamedPlayersData.name + ") rename. Reassigning associated data");
        if (AllowListFileManager.remove(renamedPlayersData.name))
            AllowListFileManager.add(newName);

        UserFileManager.remove(renamedPlayersData.name);
        renamedData(newName, renamedPlayersData);
        return true;
    }

    private static void renamedData(String newName, PersistentUserData old) {
        new PersistentUserData(newName, old);
    }

    public static PremiumData getPremiumData(PersistentUserData data) {
        return data == null ? PremiumData.UNKNOWN : data.getPremiumData();
    }

    public static boolean isRegistered(PersistentUserData data) {
        return data != null && data.getPassword().isSet();//registered - data exists and password is set
    }

    public static PersistentUserData from(String data) {
        return new PersistentUserData(ensureSplitDataCorrectness(splitPersistentData(data)));
    }

    public static PersistentUserData createDefault(String name, InetAddress ip, Password password) {
        return new PersistentUserData(name, ip, password);
    }

    public static PersistentUserData createFromPremiumInfo(String name, InetAddress ip, PremiumData premiumData) {
        //By creating a password we ensure the account cannot be stolen (in case of status reset)
        PersistentUserData data = new PersistentUserData(name, ip, Password.createRandom());
        data.setLoginType(LoginType.COMMAND);
        data.setPremiumData(premiumData);

        return data;
    }

    private static String[] ensureSplitDataCorrectness(String[] splitData) {
        int correctLength = PersistentUserData.CURRENT_DATA_LENGTH;
        int splitDataLength = splitData.length;
        int diff = correctLength - splitDataLength;

        if (diff == 0) return splitData;

        String[] correctData = new String[correctLength];

        System.arraycopy(splitData, 0, correctData, 0, splitDataLength);
        Arrays.fill(correctData, splitDataLength, correctLength, NO_VALUE);

        return correctData;
    }

    private static String[] splitPersistentData(String data) {
        return data.split("\\|");
    }

    public String emailSavable() {
        var email = this.email;
        return email == null ? NO_VALUE : email.toSavable();
    }

    @Override
    public String getName() {
        return name;
    }

    public Password getPassword() {
        return loginParams.getPassword();
    }

    public LoginParams getLoginParams() {
        return loginParams;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public InetAddress getSavedIP() {
        return ip;
    }

    public AlixLocationList getHomes() {
        return homes;
    }

    public PremiumData getPremiumData() {
        return premiumData;
    }

    public long getMutedUntil() {
        return mutedUntil;
    }

    @Override
    public long getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(long lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;

        database.updateLastSuccessfulLoginByName(this.name, lastSuccessfulLogin);
    }

    public void updateLastSuccessfulLoginTime() {
        this.setLastSuccessfulLogin(System.currentTimeMillis());
    }

    public LoginType getLoginType() {
        return this.loginParams.getLoginType();
    }

    public void setLoginType(LoginType loginType) {
        this.loginParams.setLoginType(loginType);
    }

    public void setMutedUntil(long mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public void setPassword(String password) {
        this.setPassword(Password.fromUnhashed(password));
    }

    public void setPassword(Password password) {
        this.setPasswordDatabase(password, this.getPassword());

        this.loginParams.setPassword(password);
    }

    private void setPasswordDatabase(Password newPass, Password oldPass) {
        database.setPassword(this.name, newPass, oldPass);
    }

    public void setPremiumData(@NotNull PremiumData premiumData) {
        this.setPremiumData0(premiumData);

        database.setPremiumData(this.name, premiumData);
    }

    private void setPremiumData0(@NotNull PremiumData premiumData) {
        this.premiumData = premiumData;

        if (premiumData.getStatus().isPremium())
            PremiumIdIndex.index(premiumData.premiumUUID());
    }

    public void resetPasswords() {
        this.loginParams.setPassword(Password.empty());
        this.loginParams.setExtraPassword(null);
        this.loginParams.setExtraLoginType(null);
        this.loginParams.setAuthSettings(AuthSetting.PASSWORD);
        this.loginParams.setHasProvenAuthAccess(false);

        database.clearPasswordPointer(this.name);
    }

    public PersistentUserData setIP(InetAddress ip) {
        var oldIp = this.ip;
        if (oldIp.equals(ip))
            return this;

        GeoIPTracker.addExisting(ip);
        GeoIPTracker.removeIP(oldIp);

        this.ip = ip;

        database.updateIpByName(this.name, ip.getHostAddress());
        return this;
    }

    @Override
    public long createdAt() {
        return createdAt;
    }

    @Override
    public boolean matchesPassword(@NotNull String str) {
        return this.getPassword().isEqualTo(str);
    }

    @Override
    public boolean hasSecondaryPassword() {
        return this.loginParams.getExtraPassword() != null;
    }

    @Override
    public boolean hasSecondaryPasswordActive() {
        return this.loginParams.getExtraLoginType() != null;
    }

    @Override
    public boolean matchesSecondaryPassword(@NotNull String str) {
        return this.hasSecondaryPassword() && this.loginParams.getExtraPassword().isEqualTo(str);
    }

    @Override
    public PremiumStatus getPremiumStatus() {
        return PremiumStatus.values()[this.premiumData.getStatus().ordinal()];
    }

    @Override
    public UUID premiumUUID() {
        return this.premiumData.premiumUUID();
    }

    public Identity identity() {
        return this.identity;
    }

    public Email getEmail() {
        return this.email;
    }
}