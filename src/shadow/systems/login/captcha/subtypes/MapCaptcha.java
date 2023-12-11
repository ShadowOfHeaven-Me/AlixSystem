package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import shadow.Main;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.packet.constructors.OutHeldItemSlotPacketConstructor;
import shadow.utils.holders.packet.constructors.OutMapPacketConstructor;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.users.offline.UnverifiedUser;
import shadow.utils.world.AlixWorldHolder;

import java.util.ArrayList;
import java.util.List;

public final class MapCaptcha extends Captcha {

    private static final int maxRotation = Main.config.getInt("map-captcha-font-max-random-rotation") % 360;
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private static final Object heldItemSlotPacket = OutHeldItemSlotPacketConstructor.construct(0);
    private final int mapViewId;
    private final Object itemPacket;
    private Object mapPacket;

    public MapCaptcha() {
        MapView mapView = Bukkit.createMap(AlixWorldHolder.getMain()); // Create a new map view
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
        this.mapPacket = captcha.mapPacket;
    }

    public void sendPackets() {
        UnverifiedUser user0 = this.user;
        if (user0 == null) return;
        Channel channel = user0.getPacketBlocker().getChannel();

        channel.write(heldItemSlotPacket);
        channel.write(this.itemPacket);
        channel.write(this.mapPacket);

        channel.flush();
    }

    @Override
    public void regenerate() {
        super.regenerate();
        this.updateMapPacket(captcha);
    }

    private void updateMapPacket(String captcha) {
        byte[] pixelsToDraw = CaptchaImageGenerator.generatePixelsToDraw(captcha, maxRotation, MapPalette::imageToBytes);

        this.mapPacket = OutMapPacketConstructor.createPacket(mapViewId, pixelsToDraw);
    }

    private static ItemStack generateNewCaptchaMapItem(MapView mapView) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();

        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName("§fCaptcha");

        if (!meta.hasMapView()) meta.setMapView(mapView);
        //for(MapRenderer renderer : mapView.getRenderers()) mapView.removeRenderer(renderer);
        //mapView.addRenderer();

        item.setItemMeta(meta);
        return item;
    }
}