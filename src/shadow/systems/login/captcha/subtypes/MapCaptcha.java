package shadow.systems.login.captcha.subtypes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import alix.common.utils.other.annotation.Dependent;
import io.netty.buffer.ByteBuf;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import shadow.systems.login.captcha.manager.CountdownTask;
import shadow.utils.holders.packet.constructors.OutMapPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

public final class MapCaptcha extends ItemBasedCaptcha {

    private static final int MAP_ID = 0;
    private static final ByteBuf itemPacket = createItemPacket(generateNewCaptchaMapItem());
    private final ByteBuf mapBuffer;

    public MapCaptcha() {
        byte[] pixelsToDraw = CaptchaImageGenerator.generateMapPixelsToDraw(captcha, maxRotation, true, true);

        this.mapBuffer = OutMapPacketConstructor.constructDynamic(MAP_ID, pixelsToDraw);
    }

    @Override
    @Dependent(clazz = CountdownTask.class, method = "#tick", reason = "Flush already invoked every 200 ms")
    public void sendPackets(UnverifiedUser user) {
        user.writeConstSilently(heldItemSlotPacket);//constant - write duplicate
        user.writeConstSilently(itemPacket);//constant - write duplicate
        user.writeSilently(this.mapBuffer);//dynamic - write itself
        //no flush, as specified in the @Dependent annotation
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

/*    @Override
    protected void finalize() throws Throwable {
        int refCnt = mapBuffer.refCnt();
        if (refCnt != 0) mapBuffer.release(refCnt);
        super.finalize();
    }*/
}