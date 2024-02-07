package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import shadow.Main;
import shadow.utils.holders.packet.constructors.OutMapPacketConstructor;

public final class MapCaptcha extends ItemBasedCaptcha {

    private static final int MAP_ID = 0;
    private static final int maxRotation = Main.config.getInt("map-captcha-font-max-random-rotation") % 360;
    private final Object itemPacket;
    private Object mapPacket;

    public MapCaptcha() {
        //MapView mapView = Bukkit.createMap(AlixWorldHolder.getMain()); // Create a new map view
        //this.renderer = CaptchaMapRenderer.createNewRenderer(mapView.getId(), captcha);
        ItemStack captchaMapItem = generateNewCaptchaMapItem();

        this.itemPacket = createSpoofedPacket(captchaMapItem);
        this.updateMapPacket(captcha);
    }

/*    public MapCaptcha(MapCaptcha captcha) {
        super(captcha);
        this.itemPacket = captcha.itemPacket;
        this.mapViewId = captcha.mapViewId;
        this.mapPacket = captcha.mapPacket;
    }*/

    @Override
    public final void sendPackets() {
        user.writeSilently(heldItemSlotPacket);
        user.writeSilently(this.itemPacket);
        user.writeAndFlushSilently(this.mapPacket);
    }

    @Override
    public final void regenerate() {
        super.regenerate();
        this.updateMapPacket(captcha);
    }

    private void updateMapPacket(String captcha) {
        byte[] pixelsToDraw = CaptchaImageGenerator.generatePixelsToDraw(captcha, maxRotation);

        this.mapPacket = OutMapPacketConstructor.construct(MAP_ID, pixelsToDraw);
    }

    private static ItemStack generateNewCaptchaMapItem() {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();

        meta.addItemFlags(ItemFlag.values());
        meta.setDisplayName("§fCaptcha");

        meta.setMapId(MAP_ID);

        item.setItemMeta(meta);
        return item;
    }
}