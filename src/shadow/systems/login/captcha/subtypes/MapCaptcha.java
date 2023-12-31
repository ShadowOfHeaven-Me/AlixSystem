package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;
import shadow.Main;
import shadow.utils.holders.packet.constructors.OutMapPacketConstructor;
import shadow.utils.world.AlixWorldHolder;

public final class MapCaptcha extends ItemBasedCaptcha {

    private static final int maxRotation = Main.config.getInt("map-captcha-font-max-random-rotation") % 360;
    private final int mapViewId;
    private final Object itemPacket;
    private Object mapPacket;

    public MapCaptcha() {
        MapView mapView = Bukkit.createMap(AlixWorldHolder.getMain()); // Create a new map view
        //this.renderer = CaptchaMapRenderer.createNewRenderer(mapView.getId(), captcha);
        ItemStack captchaMapItem = generateNewCaptchaMapItem(mapView);
        this.mapViewId = mapView.getId();

        this.itemPacket = createSpoofedPacket(captchaMapItem);
        this.updateMapPacket(captcha);
    }

/*    public MapCaptcha(MapCaptcha captcha) {
        super(captcha);
        this.itemPacket = captcha.itemPacket;
        this.mapViewId = captcha.mapViewId;
        this.mapPacket = captcha.mapPacket;
    }*/

    void sendPackets(Channel channel) {
        //AlixVoidPromise promise = new AlixVoidPromise(channel);
        channel.write(heldItemSlotPacket);//, promise);
        channel.write(this.itemPacket);//, promise);
        channel.write(this.mapPacket);//, promise);

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