package alix.common.data.loc.impl.bukkit;

import alix.common.data.loc.AlixLocationList;

public final class BukkitHomeList implements AlixLocationList {

    private static final BukkitNamedLocation[] EMPTY_ARRAY = new BukkitNamedLocation[0];
    private BukkitNamedLocation[] homes;

    public BukkitHomeList() {
        this.homes = EMPTY_ARRAY;
    }

    public BukkitHomeList(String savableNamedLocations) {
        if (savableNamedLocations.equals("0")) {
            this.homes = EMPTY_ARRAY;
            return;
        }
        String[] b = savableNamedLocations.split(";");
        int i = 0;
        BukkitNamedLocation[] array = new BukkitNamedLocation[b.length];
        for (String c : b) {
            BukkitNamedLocation d = BukkitNamedLocation.fromString(c);
            if (d != null) array[i++] = d;
        }
        if (b.length != i) {//Some of the homes were obstructed or could not be found (usually happens when a world is deleted)
            this.homes = new BukkitNamedLocation[i];
            System.arraycopy(array, 0, homes, 0, i);
            //currentIndex = i;
            return;
        }
        this.homes = array;
        //currentIndex = l;
    }

    public String toSavable() {
        if (homes.length == 0) return "0";
        StringBuilder a = new StringBuilder();
        for (BukkitNamedLocation b : homes) a.append(b.toString()).append(';');
        return a.substring(0, a.length() - 1);
    }

    public void addHome(BukkitNamedLocation home) {
        growByOne();
        this.homes[homes.length - 1] = home;
    }

    public void removeHome(int index) {
        int lM1 = homes.length - 1;
        if (lM1 == 0) {//don't write a switch case to include 1 as well, since it's still fast af
            this.homes = EMPTY_ARRAY;
            return;
        }
        BukkitNamedLocation[] array = new BukkitNamedLocation[lM1];

        if (lM1 == index) System.arraycopy(this.homes, 0, array, 0, lM1);
        else for (int i = 0; i < homes.length; i++) array[i] = this.homes[i == index ? ++i : i];

        this.homes = array;
        //this.currentIndex--;
    }

    private void growByOne() {
        BukkitNamedLocation[] array = new BukkitNamedLocation[homes.length + 1];
        System.arraycopy(homes, 0, array, 0, homes.length);
        this.homes = array;
    }

    public int indexOf(String name) {
        for (int i = 0; i < homes.length; i++)
            if (homes[i].getName().equals(name)) return i;
        return -1;
    }

    public BukkitNamedLocation getByName(String name) {
        for (BukkitNamedLocation home : homes)
            if (name.equals(home.getName())) return home;
        return null;
    }

    public void setHome(int i, BukkitNamedLocation home) {
        homes[i] = home;
    }

    public BukkitNamedLocation[] array() {
        return homes;
    }
}