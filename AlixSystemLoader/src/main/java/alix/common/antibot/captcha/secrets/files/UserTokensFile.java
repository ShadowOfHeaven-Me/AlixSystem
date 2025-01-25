package alix.common.antibot.captcha.secrets.files;

import alix.common.utils.file.AlixFileManager;
import alix.common.utils.other.keys.secret.MapSecretKey;
import alix.common.utils.other.keys.secret.reader.KeyReader;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserTokensFile extends AlixFileManager {

    private final KeyReader keyReader = KeyReader.uuidReaderImpl();
    private final Map<MapSecretKey, String> map = new ConcurrentHashMap();

    UserTokensFile() {
        super("user-tokens", FileType.SECRET);
    }

    @Override
    protected void loadLine(String line) {
        String[] a = line.split("\\|", 2);
        this.map.put(this.keyReader.readKey(a[0]), a[1]);
    }

    public Map<MapSecretKey, String> getMap() {
        return map;
    }

    void save() {
        try {
            super.saveKeyAndVal(this.map, "|", null, MapSecretKey::savableKey, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}