package alix.velocity.systems.packets.anvil;

import alix.velocity.systems.packets.gui.AbstractAlixGUI;
import alix.velocity.systems.packets.gui.inv.AbstractInventory;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import ua.nanit.limbo.connection.login.gui.AbstractAnvilBuilder;
import ua.nanit.limbo.connection.login.gui.AnvilBuilderGoal;
import ua.nanit.limbo.connection.login.packets.SoundPackets;

import java.util.function.Consumer;

public final class VerifiedAnvilBuilder extends AbstractAnvilBuilder<VerifiedAnvilBuilder> implements AbstractAlixGUI, AbstractInventory {

    private final VerifiedUser user;
    private final Runnable returnOriginalGui;
    private final Consumer<String> onValidConfirmation;

    public VerifiedAnvilBuilder(VerifiedUser user, Runnable returnOriginalGui, Consumer<String> onValidConfirmation, AnvilBuilderGoal goal) {
        super(user.getChannel(), user.getVersion(), goal, self -> self.user.getChannel().flush());
        this.user = user;
        this.returnOriginalGui = returnOriginalGui;
        this.onValidConfirmation = onValidConfirmation;
    }

    @Override
    public void select(int slot) {
        switch (slot) {
            case 1:
                this.returnOriginalGui.run();
                return;
            case 2:
                if (isPasswordValid) this.onValidConfirmation.accept(this.input);
                else {
                    //this.user.write(SoundPackets.VILLAGER_NO);
                    this.user.writePacketSilently(SoundPackets.wrapperOf(Sounds.ENTITY_VILLAGER_NO));
                    if (this.invalidityReason != null) this.user.sendMessage(this.invalidityReason);
                    else this.user.flush();
                }
        }
    }

    @Override
    public VerifiedUser getUser() {
        return this.user;
    }

    @Override
    public void spoofItems() {
        this.spoofValidAccordingly();
    }

    @Override
    public void onClick(WrapperPlayClientClickWindow packet) {
        this.select(packet.getSlot());
    }

    @Override
    public AbstractInventory getGUI() {
        return this;
    }

    @Override
    public void setItem(int i, ItemStack item) {//NOOP
    }

    @Override
    public int getSize() {//not important
        return 0;
    }
}