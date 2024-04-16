package shadow.systems.login.captcha.subtypes;

import io.netty.buffer.ByteBuf;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.packet.constructors.OutHeldItemSlotPacketConstructor;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.users.types.UnverifiedUser;

import java.util.ArrayList;
import java.util.List;

abstract class ItemBasedCaptcha extends Captcha {

    static final ByteBuf heldItemSlotPacket = OutHeldItemSlotPacketConstructor.construct(0);
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private static final ByteBuf EMPTY_INVENTORY_PACKET = createItemPacket(AIR);
    //final Object itemPacket;

    ItemBasedCaptcha() {
    }

    static ByteBuf createItemPacket(ItemStack captchaItem) {
        List<ItemStack> list = new ArrayList<>(45);
        for (int i = 0; i <= 45; i++) list.add(AIR);
        list.set(36, captchaItem);//the slot id 36 is the 0 slot in the hotbar

        return OutWindowItemsPacketConstructor.constructConst(0, list);
    }

    @Override
    public final void onCompletion(UnverifiedUser user) {
        user.writeAndFlushConstSilently(EMPTY_INVENTORY_PACKET);
    }
}