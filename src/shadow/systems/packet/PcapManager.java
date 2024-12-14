package shadow.systems.packet;

import org.bukkit.Bukkit;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import shadow.Main;

import java.net.InetAddress;
import java.util.List;

public final class PcapManager {

    public static void init() {
        new Thread(PcapManager::init0).start();
    }

    private static void init0() {
        try {
            // List all available network interfaces
            List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
            if (devices == null || devices.isEmpty()) {
                Main.logError("No network interfaces found.");
                return;
            }

            Main.logError("Available interfaces:");
            for (int i = 0; i < devices.size(); i++) {
                System.out.printf("[%d] %s%n", i, devices.get(i));
            }

            // Select the first interface (modify as needed)

            InetAddress serverAddr = InetAddress.getByName(Bukkit.getIp());
            PcapNetworkInterface[] nifs = devices.stream().filter(d -> d.getAddresses().stream().anyMatch(adr -> adr.getAddress().equals(serverAddr))).toArray(PcapNetworkInterface[]::new);

            Main.logError("Found NIFS: " + nifs.length);

            for (PcapNetworkInterface in : nifs) {
                Main.logError("WWWWWWWWW: " + in);
            }


            PcapNetworkInterface nif = nifs[0];
            Main.logError("Using interface: " + nif.getName());

            // Open a live capture handle on the selected interface
            PcapHandle handle = nif.openLive(
                    65536, // Snapshot length
                    PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, // Promiscuous mode
                    10 // Timeout in milliseconds
            );

            // Apply a packet filter (optional)
            String filter = "tcp[tcpflags] & tcp-syn != 0";
            handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);
            Main.logError("Filter applied: " + filter);

            // Start non-blocking packet capture
            Main.logError("Starting packet capture...");
            PacketListener listener = packet -> {
                Main.logError("Packet captured: " + packet);

                // Parse IPv4 packets
                IpV4Packet ipv4Packet = packet.get(IpV4Packet.class);
                if (ipv4Packet != null) {
                    Main.logError("IPv4 Packet - Source: " + ipv4Packet.getHeader().getSrcAddr() +
                            ", Destination: " + ipv4Packet.getHeader().getDstAddr());

                    // Parse TCP packets
                    TcpPacket tcpPacket = packet.get(TcpPacket.class);
                    if (tcpPacket != null) {
                        Main.logError("TCP Packet - Source Port: " + tcpPacket.getHeader().getSrcPort() +
                                ", Destination Port: " + tcpPacket.getHeader().getDstPort());
                    }
                }
            };

            // Start the capture in a separate thread
            new Thread(() -> {
                try {
                    handle.loop(-1, listener); // Capture indefinitely
                } catch (InterruptedException e) {
                    Main.logError("Packet capture interrupted.");
                } catch (PcapNativeException | NotOpenException e) {
                    e.printStackTrace();
                }
            }).start();

            // Let the main thread continue running
            //Main.logError("Press Enter to stop capturing...");
            //System.in.read(); // Wait for user input to stop

            // Stop the packet capture and close the handle
            //handle.breakLoop(); // Stop capturing
            //captureThread.join(); // Wait for the capture thread to finish
            //handle.close(); // Clean up resources
            //Main.logError("Packet capture stopped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}