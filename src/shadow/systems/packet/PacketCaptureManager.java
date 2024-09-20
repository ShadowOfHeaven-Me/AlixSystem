/*
package shadow.systems.packet;

import alix.common.utils.other.throwable.AlixException;
import lombok.SneakyThrows;
import net.elytrium.pcap.Pcap;
import net.elytrium.pcap.PcapNative;
import net.elytrium.pcap.handle.BpfProgram;
import net.elytrium.pcap.handle.PcapDumper;
import net.elytrium.pcap.handle.PcapHandle;
import net.elytrium.pcap.layer.Packet;
import net.elytrium.pcap.layer.data.LinkType;
import net.elytrium.pcap.layer.exception.LayerDecodeException;
import org.bukkit.Bukkit;
import shadow.Main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class PacketCaptureManager implements Runnable {

    //Source code: https://github.com/Elytrium/pcap-java

    private final PcapHandle handle;
    private final PcapDumper dumper;
    private final LinkType dataLink;

    @SneakyThrows
    private PacketCaptureManager() {
        this.handle = Pcap.openLive("any", 120, 1, 10);
        this.dumper = this.handle.dumpOpen("dump.pcap");
        this.dataLink = this.handle.datalink();

        BpfProgram filter = this.handle.compile("tcp and dst port " + Bukkit.getPort(), 1);
        this.handle.setFilter(filter);
        filter.free();
    }

    private void start() {
        new Thread(this).start();
    }

    @SneakyThrows
    @Override
    public void run() {
        this.handle.loop(-1, (bufferHeader, rawBuffer) -> {
            try {
                Packet packet = new Packet();
                packet.decode(rawBuffer, this.dataLink);

                Main.logInfo("PACKET: " + packet + " HEADER: " + bufferHeader);

            } catch (LayerDecodeException e) {
                throw new AlixException(e);
            }
        });
    }

    public static void init() {
        new PacketCaptureManager().start();
    }

    static {
        try {
            Pcap.init();
        } catch (Throwable e) {
            try {
                System.loadLibrary("pcap-native");
            } catch (UnsatisfiedLinkError e2) {
                try (InputStream inputStream = PcapNative.class.getResourceAsStream("/libpcap-native.so")) {
                    if (inputStream == null) {
                        throw new IOException();
                    }

                    File directory = Files.createTempDirectory("pcap-native").toFile();
                    File libraryFile = new File(directory, "libpcap-native.so");
                    Files.copy(inputStream, libraryFile.toPath());
                    Main.logWarning("FILE EXISTTTT " + libraryFile.exists());
                    //libraryFile.createNewFile();
                    System.load(libraryFile.getAbsolutePath());
                    libraryFile.deleteOnExit();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw e2;
                }
            }
        }
    }
}*/
