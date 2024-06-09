package shadow.utils.objects.savable.multi;

import shadow.utils.objects.savable.loc.NamedLocation;

import static shadow.utils.main.AlixUtils.split;

public final class HomeList {

    private static final NamedLocation[] EMPTY_ARRAY = new NamedLocation[0];
    private NamedLocation[] homes;

    public HomeList() {
        this.homes = EMPTY_ARRAY;
    }

    public HomeList(String savableNamedLocations) {
        if (savableNamedLocations.equals("0")) {
            this.homes = EMPTY_ARRAY;
            return;
        }
        String[] b = split(savableNamedLocations, ';');
        int i = 0;
        NamedLocation[] array = new NamedLocation[b.length];
        for (String c : b) {
            NamedLocation d = NamedLocation.fromString(c);
            if (d != null) array[i++] = d;
        }
        if (b.length != i) {//Some of the homes were obstructed or could not be found (usually happens when a world is deleted)
            this.homes = new NamedLocation[i];
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
        for (NamedLocation b : homes) a.append(b.toString()).append(';');
        return a.substring(0, a.length() - 1);
    }

    public void addHome(NamedLocation home) {
        growByOne();
        this.homes[homes.length - 1] = home;
    }

    public void removeHome(int index) {
        int lM1 = homes.length - 1;
        if (lM1 == 0) {//don't write a switch case to include 1 as well, since it's still fast af
            this.homes = EMPTY_ARRAY;
            return;
        }
        NamedLocation[] array = new NamedLocation[lM1];

        if (lM1 == index) System.arraycopy(this.homes, 0, array, 0, lM1);
        else for (int i = 0; i < homes.length; i++) array[i] = this.homes[i == index ? ++i : i];

        this.homes = array;
        //this.currentIndex--;
    }

    private void growByOne() {
        NamedLocation[] array = new NamedLocation[homes.length + 1];
        System.arraycopy(homes, 0, array, 0, homes.length);
        this.homes = array;
    }

    public int indexOf(String name) {
        for (int i = 0; i < homes.length; i++)
            if (homes[i].getName().equals(name)) return i;
        return -1;
    }

    public NamedLocation getByName(String name) {
        for (NamedLocation home : homes)
            if (name.equals(home.getName())) return home;
        return null;
    }

    public void setHome(int i, NamedLocation home) {
        homes[i] = home;
    }

    public NamedLocation[] array() {
        return homes;
    }
}