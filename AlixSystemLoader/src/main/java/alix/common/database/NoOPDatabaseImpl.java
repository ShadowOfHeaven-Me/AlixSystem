package alix.common.database;

import alix.common.data.security.password.Password;

import java.util.UUID;
import java.util.function.LongConsumer;

final class NoOPDatabaseImpl implements DatabaseUpdater {
    @Override
    public void createTablesSync() {

    }

    @Override
    public void insertUser(String name, UUID uuid, long createdAt) {

    }

    @Override
    public void addPassword(String ownerName, String hashedPassword, byte hashId, String salt, byte matcherId, LongConsumer onSuccess) {

    }

    @Override
    public void addPasswordAndLink(String ownerName, Password password) {

    }

    @Override
    public void setPasswordPointer(String name, Long passwordId) {

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
    public void updatePasswordById(long passwordId, String hashedPassword, byte hashId, String salt, byte matcherId) {

    }

    @Override
    public void updatePasswordByOwner(String ownerName, Password password) {

    }
}