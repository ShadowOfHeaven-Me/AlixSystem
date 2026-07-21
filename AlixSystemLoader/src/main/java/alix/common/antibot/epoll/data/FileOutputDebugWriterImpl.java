package alix.common.antibot.epoll.data;

import alix.common.AlixCommonMain;
import alix.common.antibot.epoll.ConnectionStats;
import alix.common.antibot.epoll.TelemetryProfilerImpl;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

final class FileOutputDebugWriterImpl implements DebugWriter {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(TelemetryProfilerImpl.ConnectionRecord.class, new ConnectionRecordSerializer())
            .create();

    @Override
    public void writeToDisk(TelemetryProfilerImpl.ConnectionRecord record) {
        AlixScheduler.asyncBlocking(() -> {
            var stats = new ConnectionStats(record.tcpSamples);
            record.stats = stats;
            //record.evaluation = TrafficHeuristics.evaluate(record, stats);

            this.lock.lock();
            try {
                this.out.write(GSON.toJson(record) + "\n");
                this.out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                this.lock.unlock();
            }
        });
    }

    @SneakyThrows
    private static BufferedWriter output() {
        var folder = new File(AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolder(), "out");
        var f = new File(folder, AlixCommonUtils.getFormattedDate(new Date()) + ".json");
        folder.mkdir();
        f.createNewFile();

        return Files.newBufferedWriter(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    private final BufferedWriter out = output();
    private final ReentrantLock lock = new ReentrantLock();

    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.out.close();
                AlixCommonMain.logInfo("Closed out");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}