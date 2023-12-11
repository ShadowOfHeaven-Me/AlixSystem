package shadow.utils.objects.savable.multi;

import shadow.utils.objects.savable.loc.NamedLocation;

import static shadow.utils.main.AlixUtils.split;

public final class HomeList {

    private NamedLocation[] homes;
    private int currentIndex;

    public HomeList() {
        this.homes = new NamedLocation[0];
    }

    public HomeList(String savableNamedLocations) {
        if (savableNamedLocations.equals("0")) {
            this.homes = new NamedLocation[0];
            return;
        }
        String[] b = split(savableNamedLocations, ';');
        final int l = b.length;
        int i = 0;
        NamedLocation[] array = new NamedLocation[l];
        for (String c : b) {
            NamedLocation d = NamedLocation.fromString(c);
            if (d != null) array[i++] = d;
        }
        if (l != i) {//Some of the NamedLocations were obstructed or could not be found (usually happens when a world is deleted)
            this.homes = new NamedLocation[i];
            System.arraycopy(array, 0, homes, 0, i);
            currentIndex = i;
            return;
        }
        this.homes = array;
        currentIndex = l;
    }

    public String toSavable() {
        if (homes.length == 0) return "0";
        StringBuilder a = new StringBuilder();
        for (NamedLocation b : homes) a.append(b.toString()).append(';');
        return a.substring(0, a.length() - 1);
    }

    public void addHome(NamedLocation home) {
        growByOne();
        this.homes[currentIndex++] = home;
    }

    public void removeHome(int index) {
        NamedLocation[] array = new NamedLocation[currentIndex - 1];
        int h = 0;
        for (int i = 0; i < currentIndex; i++)
            if (i != index) array[h++] = homes[i];
        this.homes = array;
        this.currentIndex--;
    }

    private void growByOne() {
        NamedLocation[] array = new NamedLocation[currentIndex + 1];
        System.arraycopy(homes, 0, array, 0, currentIndex);
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

    public NamedLocation[] asArray() {
        return homes;
    }
}