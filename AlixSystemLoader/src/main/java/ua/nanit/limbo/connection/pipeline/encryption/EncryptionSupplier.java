package ua.nanit.limbo.connection.pipeline.encryption;

import ua.nanit.limbo.connection.ClientConnection;

public interface EncryptionSupplier {

    CipherHandler getHandlerFor(ClientConnection connection);

}