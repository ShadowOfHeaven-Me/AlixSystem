package ua.nanit.limbo.connection.captcha.xes;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.netty.util.concurrent.ScheduledFuture;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.play.entity.PacketPlayOutSpawnEntity;
import ua.nanit.limbo.protocol.packets.play.map.PacketPlayOutMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class SpinningDonut implements Runnable {

    private static final Color[] colors = {
            new Color(255, 255, 255), // White
            new Color(234, 234, 234),
            new Color(213, 213, 213),
            new Color(192, 192, 192),
            new Color(171, 171, 171),
            new Color(150, 150, 150),
            new Color(129, 129, 129),
            new Color(108, 108, 108),
            new Color(87, 87, 87),
            new Color(76, 76, 76),
            new Color(69, 69, 69),
            new Color(64, 64, 64)  // Dark gray
    };

    private final int mapsTotal, mapsInOneDim;
    private final ClientConnection connection;
    private final ScheduledFuture<?> task;
    private double A = 0, B = 0; // Angles for rotation

    private SpinningDonut(ClientConnection connection) {
        this.connection = connection;

        BufferedImage image = createSpinningDonutImg();

        //how many maps are in each dimension of the square
        this.mapsInOneDim = image.getWidth() / 128;
        //the total amount of maps in the square
        this.mapsTotal = this.mapsInOneDim * this.mapsInOneDim;

        List<PacketOut> list = createSpinningDonut(image);
        list.forEach(connection::writePacket);
        connection.flush();

        this.task = connection.getChannel().eventLoop().scheduleAtFixedRate(this, 50, 50, TimeUnit.MILLISECONDS);
        connection.getChannel().closeFuture().addListener(f -> this.task.cancel(false));
    }

    @Override
    public void run() {
        int mapId = 0;
        BufferedImage image = createSpinningDonutImg();
        for (int i = 0; i < mapsTotal; i++) {

            int x = i % mapsInOneDim;
            int y = i / mapsInOneDim;

            int imageX = x * 128;
            int imageY = y * 128;

            BufferedImage subImage = image.getSubimage(imageX, imageY, 128, 128);

            this.connection.writePacket(createMap(mapId++, subImage));
        }
        this.connection.flush();
    }

    public static void spinningDonut(ClientConnection connection) {
        /*if (true) {
            WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(1, Optional.of(UUID.randomUUID()), EntityTypes.ITEM_FRAME, new Vector3d(0, 60, -2), 0, 0, 0, BlockFace.NORTH.getFaceValue(), Optional.of(Vector3d.zero()));

            connection.writeAndFlushPacket(new PacketPlayOutSpawnEntity(entity));
            return;
        }*/
        new SpinningDonut(connection);
    }

    private List<PacketOut> createSpinningDonut(BufferedImage image) {
        //the rendering pos in the very middle
        Vector3d middle = new Vector3d(0, 61, -2);

        assert image.getWidth() == image.getHeight();
        List<PacketOut> list = new ArrayList<>(mapsTotal * 3);

        int halfX = mapsInOneDim / 2;
        int halfY = mapsInOneDim / 2;

        int entityId = 1;
        int mapId = 0;

        for (int i = 0; i < mapsTotal; i++) {

            int x = i % mapsInOneDim;
            int y = i / mapsInOneDim;

            int imageX = x * 128;
            int imageY = y * 128;

            BufferedImage subImage = image.getSubimage(imageX, imageY, 128, 128);

            Vector3d pos = middle.add(x - halfX, -y + halfY, 0);
            createEntityMap(pos, subImage, entityId++, mapId++, list);
        }
        return list;
    }

    static void createEntityMap(Vector3d pos, BufferedImage image, int entityId, int mapId, List<PacketOut> list) {
        ItemStack item = ItemStack.builder().type(ItemTypes.FILLED_MAP).component(ComponentTypes.MAP_ID, mapId).build();

        WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ITEM_FRAME, pos, 0, 0, 0, BlockFace.SOUTH.getFaceValue(), Optional.of(Vector3d.zero()));
        //https://wiki.vg/Entity_metadata#Item_Frame
        /*WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId,
                Arrays.asList(new EntityData(8, EntityDataTypes.ITEMSTACK, item),
                        new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20)));//invisible*/

        list.add(new PacketPlayOutSpawnEntity(entity));
        //list.add(new PacketPlayOutEntityMetadata(metadata));
        list.add(createMap(mapId, image));
    }

    static PacketPlayOutMap createMap(int mapId, byte[] bytes, int columns, int rows) {
        int offsetX = 0;
        int offsetZ = 0;
        WrapperPlayServerMapData map = new WrapperPlayServerMapData(mapId, (byte) 0, false, false, null, columns, rows, offsetX, offsetZ, bytes);
        return new PacketPlayOutMap(map);
    }

    static PacketPlayOutMap createMap(int mapId, BufferedImage image, int columns, int rows) {
        return createMap(mapId, CaptchaImageGenerator.imageToBytes(image), columns, rows);
    }

    static PacketPlayOutMap createMap(int mapId, BufferedImage image) {
        return createMap(mapId, image, image.getWidth(), image.getHeight());
    }

    private BufferedImage createSpinningDonutImg() {
        int width = 512;
        int height = 512;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Clear the image
        Graphics2D g2d = image.createGraphics();
        //g2d.setColor(Color.BLACK);
        g2d.setColor(CaptchaImageGenerator.NO_COLOR);
        g2d.fillRect(0, 0, width, height);

        // Arrays for storing z values and characters
        double[] z = new double[1760];
        char[] b = new char[1760];
        int[] colorIndices = new int[1760]; // To store the color index for each pixel
        Arrays.fill(b, ' ');
        Arrays.fill(z, 0);

        // Light source direction (normalized vector)
        double lx = 0, ly = 1, lz = -1; // Light coming from above and slightly behind

        // Generate the donut using the exact algorithm from donut.c
        for (double j = 0; j < 6.28; j += 0.07) {
            for (double i = 0; i < 6.28; i += 0.02) {
                double c = Math.sin(i), d = Math.cos(j), e = Math.sin(A), f = Math.sin(j), g = Math.cos(A), h = d + 2, D = 1 / (c * h * e + f * g + 5), l = Math.cos(i), m = Math.cos(B), n = Math.sin(B), t = c * h * g - f * e;

                // Surface normal vector
                double ny = l * h * n + t * m;
                double nz = c * h * e + f * g;

                // Compute brightness using dot product with light source
                double nx = (l * h * m - t * n);
                double brightness = (nx * lx + ny * ly + nz * lz) / Math.sqrt(nx * nx + ny * ny + nz * nz);
                int N = (int) (brightness * 8); // Scale brightness to index range

                int x = (int) (40 + 30 * D * (l * h * m - t * n)), y = (int) (12 + 15 * D * (l * h * n + t * m)), o = x + 80 * y;//,
                //N = (int) (8 * ((f * e - c * d * g) * m - c * d * e - f * g - l * d * n));

                if (y > 0 && y < 22 && x > 0 && x < 80 && D > z[o]) {
                    z[o] = D;
                    b[o] = '@'; //chars[Math.max(N, 0)];
                    colorIndices[o] = Math.max(N, 0) % colors.length; // Map brightness to color index
                }
            }
        }

        // Calculate offsets to center the donut
        int offsetX = (width - 80 * 10) / 2;  // Center the donut horizontally
        int offsetY = (height - 22 * 10) / 4; // Center the donut vertically

        // Draw the characters to the image
        for (int k = 0; k < 1760; k++) {
            int x = k % 80;
            int y = k / 80;
            if (x > 0 && x < width && y > 0 && y < height) {
                //g2d.setColor(getRainbowColor(colorIndices[k]));
                g2d.setColor(colors[colorIndices[k]]);
                //g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(b[k]), offsetX + x * 10, offsetY + y * 20);
            }
        }
        g2d.dispose();

        // Update angles for rotation
        A += 0.04;
        B += 0.02;

        return image;
    }
}