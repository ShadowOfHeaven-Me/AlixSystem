package shadow.systems.login.captcha.subtypes;

import io.netty.channel.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class BookCaptcha extends ItemBasedCaptcha {

    private Object itemPacket;

    public BookCaptcha() {
        this.regenPackets();
    }

    @Override
    void sendPackets(Channel channel) {
    }

    @Override
    public void regenerate() {
        super.regenerate();
        this.regenPackets();
    }

    private void regenPackets() {
        this.itemPacket = createSpoofedPacket(generateNewCaptchaBookItem(this.captcha));
    }

    private static ItemStack generateNewCaptchaBookItem(String captcha) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();

        TextComponent text = new TextComponent("Click the text below \n");
        BaseComponent[] verify = new ComponentBuilder("[Verify]").color(ChatColor.GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/captcha " + captcha)).create();
        for (BaseComponent c : verify) text.addExtra(c);

        meta.spigot().addPage(new BaseComponent[0]);
        meta.spigot().setPage(1, text);
        return item;
        //meta.setPages(Collections.singletonList("Click the text below \n" + text.retain());
    }
}