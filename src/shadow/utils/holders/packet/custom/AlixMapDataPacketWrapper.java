package shadow.utils.holders.packet.custom;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import io.netty.buffer.ByteBuf;
import shadow.utils.netty.NettyUtils;

public final class AlixMapDataPacketWrapper {/*extends PacketWrapper<AlixMapDataPacketWrapper> {

    private final int mapId;
    private final byte[] pixels;

    public AlixMapDataPacketWrapper(int mapId, byte[] pixels) {
        super(PacketType.Play.Server.MAP_DATA);

        this.mapId = mapId;
        this.pixels = pixels;
    }*/


    /*if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_19)) {
        //ByteBufHelper.writeVarInt(buffer, 0);
        ByteBufHelper.writeVarInt(buffer, 0);
        ByteBufHelper.writeByte(buffer, 0);
        ByteBufHelper.writeByte(buffer, 0);
        ByteBufHelper.writeByte(buffer, 0);
        ByteBufHelper.writeBoolean(buffer, false);//has display name
    }*/
    //int packetId = PacketType.Play.Server.MAP_DATA.getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
    private static final ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
    private static final int PACKET_ID = PacketType.Play.Server.MAP_DATA.getId(serverVersion.toClientVersion());

    public static ByteBuf createBuffer(int mapId, byte[] pixels) {
        ByteBuf buffer = NettyUtils.buffer();
        ByteBufHelper.writeVarInt(buffer, PACKET_ID);//packed id
        ByteBufHelper.writeVarInt(buffer, mapId);//map id
        ByteBufHelper.writeByte(buffer, 3);//scale
        ByteBufHelper.writeBoolean(buffer, false);//has icons/locked
        ByteBufHelper.writeBoolean(buffer, false);//locked/tracking positions

        //Only difference in protocol after 1.8.8 is between these two versions
        //https://wiki.vg/index.php?title=Protocol&oldid=17753#Map_Data 1.19
        //https://wiki.vg/index.php?title=Protocol&oldid=18067#Map_Data 1.19.2
        /*if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_19))//the array is not optional on 1.19 and below
            ByteBufHelper.writeVarInt(buffer, 0);//just say that the length is 0, so that we don't have to provide the array*/

        ByteBufHelper.writeByte(buffer, 128);//updated columns
        ByteBufHelper.writeByte(buffer, 128);//updated rows
        ByteBufHelper.writeByte(buffer, 0);//x offset
        ByteBufHelper.writeByte(buffer, 0);//z offset
        ByteBufHelper.writeVarInt(buffer, pixels.length);//byte array's length, so here 16384
        ByteBufHelper.writeBytes(buffer, pixels);//write the byte array (map's pixels)
        return buffer;
    }
/*
    @Override
    public void write() {
        this.writeVarInt(this.mapId);
        this.writeByte(3);//scale
        this.writeBoolean(false);//has icons
        this.writeBoolean(false);//locked
        this.writeByte(128);//updated columns
        this.writeByte(128);//updated rows
        this.writeByte(0);//x offset
        this.writeByte(0);//z offset
        this.writeVarInt(this.pixels.length);//so here 16384
        this.writeByteArray(this.pixels);
    }*/
}