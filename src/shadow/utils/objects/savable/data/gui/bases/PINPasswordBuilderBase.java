package shadow.utils.objects.savable.data.gui.bases;

import org.bukkit.entity.Player;
import shadow.systems.gui.AlixGUI;
import shadow.systems.gui.item.GUIItem;
import shadow.utils.objects.savable.data.gui.PasswordGui;

import static shadow.utils.main.AlixUtils.sendMessage;

public class PINPasswordBuilderBase extends AlixGUI {

    protected PINPasswordBuilderBase(Player player) {
        super(PasswordGui.getPinGuiCloned(PasswordGui.pinGUITitle), player);
    }

    @Override
    protected GUIItem[] create(Player player) {
        return new GUIItem[0];
    }
}