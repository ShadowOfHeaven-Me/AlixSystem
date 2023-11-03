package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;
import shadow.Main;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.packet.constructors.OutHeldItemSlotPacketConstructor;
import shadow.utils.holders.packet.constructors.OutMapPacketConstructor;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.ArrayList;
import java.util.List;

public final class MapCaptcha extends Captcha {

    private static final int maxRotation = Main.config.getInt("map-captcha-font-max-random-rotation") % 360;
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private static final Object heldItemSlotPacket = OutHeldItemSlotPacketConstructor.construct(0);
    private final int mapViewId;
    private final Object itemPacket;
    private Object outMapPacket;

    public MapCaptcha() {
        MapView mapView = Bukkit.createMap(defaultWorld); // Create a new map view
        //this.renderer = CaptchaMapRenderer.createNewRenderer(mapView.getId(), captcha);
        ItemStack captchaMapItem = generateNewCaptchaMapItem(mapView);
        this.mapViewId = mapView.getId();

        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 36; i++) list.add(AIR);
        list.add(captchaMapItem);

        this.itemPacket = OutWindowItemsPacketConstructor.construct(0, 37, list);
        this.updateMapPacket(captcha);
    }

    public MapCaptcha(MapCaptcha captcha) {
        super(captcha);
        this.itemPacket = captcha.itemPacket;
        this.mapViewId = captcha.mapViewId;
        this.outMapPacket = captcha.outMapPacket;
    }

    public void sendPackets() {
        if (user == null) return;
        Channel channel = user.getPacketBlocker().getChannel();

        channel.write(this.itemPacket);
        channel.write(heldItemSlotPacket);
        channel.write(outMapPacket);
        channel.flush();
    }

    @Override
    public Captcha inject(UnverifiedUser user) {
        this.user = user;
        return super.inject(user);
    }

    @Override
    public void uninject() {
        this.user = null;
        super.uninject();
    }

    @Override
    public void regenerate() {
        super.regenerate();
        this.updateMapPacket(captcha);
    }

    private void updateMapPacket(String captcha) {
        byte[] pixelsToDraw = CaptchaImageGenerator.generatePixelsToDraw(captcha, maxRotation, MapPalette::imageToBytes);

        this.outMapPacket = OutMapPacketConstructor.createPacket(mapViewId, pixelsToDraw);
    }

    private static final World defaultWorld = Bukkit.getWorlds().get(0);

    private static ItemStack generateNewCaptchaMapItem(MapView mapView) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName("§fCaptcha");

        if (!meta.hasMapView()) meta.setMapView(mapView);

        item.setItemMeta(meta);
        return item;
    }
}