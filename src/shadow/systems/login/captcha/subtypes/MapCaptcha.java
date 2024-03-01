package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import alix.common.utils.other.annotation.Dependent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import shadow.Main;
import shadow.systems.login.captcha.manager.CountdownTask;
import shadow.utils.holders.packet.constructors.OutMapPacketConstructor;
import shadow.utils.users.offline.UnverifiedUser;

public final class MapCaptcha extends ItemBasedCaptcha {

    private static final int MAP_ID = 0;
    private static final int maxRotation = Main.config.getInt("map-captcha-max-random-rotation") % 360;
    private static final ItemStack captchaMapItem = generateNewCaptchaMapItem();
    private static final Object itemPacket = createItemPacket(captchaMapItem);
    private final Object mapPacket;

    public MapCaptcha() {
        byte[] pixelsToDraw = CaptchaImageGenerator.generatePixelsToDraw(captcha, maxRotation);

        this.mapPacket = OutMapPacketConstructor.construct(MAP_ID, pixelsToDraw);
    }

    @Override
    @Dependent(clazz = CountdownTask.class, method = "#tick", reason = "Flush already invoked every 200 ms")
    public final void sendPackets(UnverifiedUser user) {
        user.writeSilently(heldItemSlotPacket);
        user.writeSilently(itemPacket);
        user.writeSilently(this.mapPacket);//instead of writeAndFlushSilently, with the reason specified in the @Dependent annotation
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