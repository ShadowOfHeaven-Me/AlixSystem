/*
package alix.velocity.server.packets;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;

public final class MapDataPacket implements MinecraftPacket {

    private static final byte MAP_SCALE = 3;
    private final int mapId;
    private final byte[] bytes;

    public MapDataPacket(String captcha, int mapId) {
        this.bytes = CaptchaImageGenerator.generatePixelsToDraw(captcha, 45);
        this.mapId = mapId;
    }

    public MapDataPacket() {
        throw new AlixException("Empty MapDataPacket constructor - should not have been invoked!");
    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        throw new AlixError();
    }

    @Override
    public void encode(ByteBuf buf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        buf.writeInt(this.mapId);
        buf.writeByte(MAP_SCALE);
        buf.writeBoolean(true);
        buf.writeBoolean(false);
        buf.writeByte(128);
        buf.writeByte(128);
        buf.writeByte(0);
        buf.writeByte(0);
        buf.writeInt(16384);
        buf.writeBytes(this.bytes);
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return false;
    }
}*/
