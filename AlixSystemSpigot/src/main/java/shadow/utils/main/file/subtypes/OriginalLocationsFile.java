package shadow.utils.main.file.subtypes;

import org.bukkit.Location;
import shadow.utils.main.file.FileManager;
import shadow.utils.objects.savable.loc.SavableLocation;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class OriginalLocationsFile extends FileManager {

    private final Map<UUID, Location> map;

    public OriginalLocationsFile() {
        super("original-locations.txt", FileType.INTERNAL);
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    protected void loadLine(String line) {
        String[] a = line.split("\\|", 2);
        this.map.put(UUID.fromString(a[0]), SavableLocation.fromStringOrSpawnIfAbsent(a[1]));
    }

    public void save() throws IOException {
        super.saveKeyAndVal(map, "|", null, null, SavableLocation::toSavableString);//l -> toSavableString(l)
    }

    public Map<UUID, Location> getMap() {
        return map;
    }
}