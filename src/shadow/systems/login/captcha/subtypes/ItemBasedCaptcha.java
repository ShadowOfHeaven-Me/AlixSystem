package shadow.systems.login.captcha.subtypes;

import io.netty.channel.Channel;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import shadow.systems.login.captcha.Captcha;
import shadow.utils.holders.packet.constructors.OutHeldItemSlotPacketConstructor;
import shadow.utils.holders.packet.constructors.OutWindowItemsPacketConstructor;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.ArrayList;
import java.util.List;

abstract class ItemBasedCaptcha extends Captcha {

    static final Object heldItemSlotPacket = OutHeldItemSlotPacketConstructor.construct(0);
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    //final Object itemPacket;

    ItemBasedCaptcha() {

    }

    static Object createSpoofedPacket(ItemStack captchaItem) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i <= 45; i++) list.add(AIR);
        list.set(36, captchaItem);//the slot id 36 is the 0 slot in the hotbar

        return OutWindowItemsPacketConstructor.construct(0, list);
    }

    @Override
    public final void sendPackets() {
        UnverifiedUser user0 = this.user;
        if (user0 == null) return;
        Channel channel = user0.getPacketBlocker().getChannel();
        this.sendPackets(channel);
    }

    abstract void sendPackets(Channel channel);
}