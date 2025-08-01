package alix.velocity.systems.packets.gui;

import alix.velocity.systems.packets.gui.inv.AbstractInventory;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

public interface AbstractAlixGUI {

    void onClick(WrapperPlayClientClickWindow packet);

    AbstractInventory getGUI();

    //AtomicInteger ID_COUNTER = new AtomicInteger();

    default void map() {
        this.getGUI().getUser().gui = this;
        this.getGUI().open();
    }
}