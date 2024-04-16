package alix.velocity.systems.captcha.impl;


import alix.common.antibot.captcha.CaptchaImageGenerator;
import alix.velocity.systems.captcha.Captcha;
import io.netty.channel.Channel;
import net.elytrium.limboapi.api.material.Item;
import net.elytrium.limboapi.api.material.VirtualItem;
import net.elytrium.limboapi.api.protocol.packets.data.MapData;
import net.elytrium.limboapi.protocol.packets.s2c.MapDataPacket;
import net.elytrium.limboapi.protocol.packets.s2c.SetSlotPacket;
import net.elytrium.limboapi.server.world.SimpleItem;

public final class MapCaptcha implements Captcha {

    //private static final Limbo limbo = AlixServer.getLimbo();
    private static final VirtualItem MAP = SimpleItem.fromItem(Item.FILLED_MAP);
    private static final SetSlotPacket itemPacket = new SetSlotPacket(0, 36, MAP, 1, 0, null);
    private final MapDataPacket mapDataPacket;

    public MapCaptcha(String captcha) {
        byte[] pixels = CaptchaImageGenerator.generatePixelsToDraw(captcha, 30, true, true);
        MapData data = new MapData(pixels);
        this.mapDataPacket = new MapDataPacket(0, (byte) 0, data);
    }

    @Override
    public void sendPackets(Channel channel) {
        channel.write(itemPacket);
        channel.writeAndFlush(mapDataPacket);

        //LimboPlayer player;
        //player.disableFalling();
        //player.writePacket(mapDataPacket);
        //player.setInventory(MAP, 0);
    }
}