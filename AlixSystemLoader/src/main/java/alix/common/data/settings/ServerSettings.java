package alix.common.data.settings;

import alix.common.utils.file.AlixFileManager;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.List;

public final class ServerSettings extends AlixFileManager {

    final Object[] data = new Object[Setting.count()];
    final List<Object> writeable = Arrays.asList(this.data);

    ServerSettings() {
        super("server-settings", FileType.INTERNAL);
    }

    int idx;
    int w;

    @SneakyThrows
    void write() {
        super.save0(this.writeable, obj -> {
            var idx = this.w++;
            return Setting.of(idx).format(this.data[idx]);
        });
        this.w = 0;
    }

    void set(Setting setting, Object val) {
        this.data[setting.ordinal()] = val;
        this.write();
    }

    @Override
    protected void loadLine(String line) {
        int idx = this.idx++;

        this.data[idx] = Setting.of(idx).parse(line);
    }
}