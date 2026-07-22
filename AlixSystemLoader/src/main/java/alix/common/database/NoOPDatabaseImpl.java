package alix.common.database;

import alix.common.data.Identity;
import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseType;

final class NoOPDatabaseImpl implements DatabaseUpdater {
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
    public void clearPasswordPointer(String name) {

    }

    @Override
    public void updateLastSuccessfulLoginByName(String name, long lastSuccessfulLogin) {

    }

    @Override
    public void updateIpByName(String name, String ip) {

    }

    @Override
    public void updatePasswordByOwner(String ownerName, Password password) {

    }

    @Override
    public void setPremiumData(String name, PremiumData data) {

    }

    @Override
    public void setPassword(String name, Password newPass, Password oldPass) {

    }

    @Override
    public void saveUserToken(Identity identity, String token) {

    }

    @Override
    public PersistentUserData loadUser(String name) {
        return null;
    }

    @Override
    public DatabaseType getType() {
        return null;
    }
}