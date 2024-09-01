package shadow.utils.misc.version;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class AlixMaterials {

    //Head
    public static final ConstItem PLAYER_HEAD = make("PLAYER_HEAD", "SKULL_ITEM", 3);
    //Wool
    public static final ConstItem YELLOW_WOOL = make("YELLOW_WOOL", "WOOL", 4);
    public static final ConstItem LIME_WOOL = make("LIME_WOOL", "WOOL", 5);
    public static final ConstItem GREEN_WOOL = make("GREEN_WOOL", "WOOL", 13);
    public static final ConstItem RED_WOOL = make("RED_WOOL", "WOOL", 14);
    public static final ConstItem BLACK_WOOL = make("BLACK_WOOL", "WOOL", 15);
    //Concrete
    public static final ConstItem GREEN_CONCRETE = make("GREEN_CONCRETE", "CONCRETE", 13);
    public static final ConstItem RED_CONCRETE = make("RED_CONCRETE", "CONCRETE", 14);
    //Sign
    public static final ConstItem OAK_SIGN = make("OAK_SIGN", "SIGN");
    //Iron bars
    public static final ConstItem IRON_BARS = make("IRON_BARS", "IRON_FENCE");
    //Command block
    public static final ConstItem COMMAND_BLOCK = make("COMMAND_BLOCK", "COMMAND");

    public static boolean isAir(Material m) {
        if (m == null) return false;//right?
        switch (m.name()) {
            case "AIR":
            case "CAVE_AIR":
            case "VOID_AIR":
                return true;
        }
        return false;
    }

    private static ConstItem make(String modernName, String oldName) {
        return make(modernName, oldName, null);
    }

    private static ConstItem make(String modernName, String oldName, Integer data) {
        ItemStack item;

        try {
            item = new ItemStack(Material.valueOf(modernName));
        } catch (Throwable ignored) {
            item = new ItemStack(Material.valueOf(oldName), 1, (short) 0, data != null ? data.byteValue() : null);
        }

        return new ConstItem(item);
    }
}