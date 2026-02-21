package alix.common.database;

import alix.common.data.premium.PremiumData;
import alix.common.data.security.password.Password;
import alix.common.database.connect.DatabaseType;

import java.util.UUID;

final class NoOPDatabaseImpl implements DatabaseUpdater {
    @Override
    public void connect() {

    }

    @Override
    public void createTablesSync() {

    }

    @Override
    public void insertUser(String name, UUID uuid, long createdAt, Password password) {

    }

    @Override
    public void addPasswordAndLink(String ownerName, Password password) {

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
    public DatabaseType getType() {
        return null;
    }
}