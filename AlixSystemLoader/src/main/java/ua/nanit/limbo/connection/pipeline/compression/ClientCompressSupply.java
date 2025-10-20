package ua.nanit.limbo.connection.pipeline.compression;

import ua.nanit.limbo.connection.ClientConnection;

public interface ClientCompressSupply {

    boolean shouldEnableCompress(ClientConnection connection);

}