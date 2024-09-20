package shadow.utils.objects.savable.data.gui;

import org.jetbrains.annotations.NotNull;
import shadow.utils.objects.savable.data.gui.virtual.VirtualInventory;

public interface AlixJavaVerificationGui extends AlixVerificationGui {

    /**
     * Throws an exception if the used gui cannot be returned
     *
     * @return The Inventory gui used for password building
     * @author ShadowOfHeaven
     */

    @NotNull
    VirtualInventory getVirtualGUI();

    /**
     * {@link AlixVerificationGui#destroy()}
     */

    @Override
    default void destroy() {
        this.getVirtualGUI().destroy();
    }

    /**
     * {@link AlixVerificationGui#openGUI()}
     */

    @Override
    default void openGUI() {
        this.getVirtualGUI().open();
    }
}