/*
package ua.nanit.limbo.connection.captcha.xes;

import alix.common.AlixCommonMain;
import alix.common.utils.file.AlixFileManager;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.SneakyThrows;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.packets.play.entity.PacketPlayOutEntityMetadata;
import ua.nanit.limbo.protocol.packets.play.entity.PacketPlayOutSpawnEntity;
import ua.nanit.limbo.protocol.packets.play.map.PacketPlayOutMap;
import ua.nanit.limbo.server.Log;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static ua.nanit.limbo.connection.captcha.xes.SpinningDonut.createMap;

public final class UiiaiuiiiaiCat implements Runnable {

    //Source code: https://github.com/DarkSavci/Minecraft-Video-Player/blob/main/src/main/java/dev/bdinc/minecraft_video_player/Main.java
    private static final float MAX_FPS = 24;
    private static final int DIV = 5;
    private static final int WIDTH = 1920 / DIV;
    private static final int HEIGHT = 1080 / DIV;
    private final ClientConnection connection;
    private final ScheduledFuture<?> task;
    private final Deque<MovieFrame> frames;

    static {
        avutil.av_log_set_level(avutil.AV_LOG_QUIET);
    }

    private UiiaiuiiiaiCat(ClientConnection connection, Deque<MovieFrame> frames) {
        this.connection = connection;
        this.frames = frames;

        List<PacketOut> list = createMapLayout();
        list.forEach(connection::writePacket);
        connection.flush();

        double fps = MAX_FPS;//grabber.getFrameRate() > MAX_FPS ? MAX_FPS : grabber.getFrameRate();
        long newFrameEveryXMillis = (long) (1000 / fps);
        this.task = connection.getChannel().eventLoop().scheduleAtFixedRate(this, newFrameEveryXMillis, newFrameEveryXMillis, TimeUnit.MILLISECONDS);

        connection.getChannel().closeFuture().addListener(f -> this.cancel());
    }

    private List<PacketOut> createMapLayout() {
        //the rendering pos in the very middle
        Vector3d middle = new Vector3d(0, 61, -1);

        List<PacketOut> list = new ArrayList<>();

        int mapsInX = roundUp(WIDTH / 128f);
        int mapsInY = roundUp(HEIGHT / 128f);

        int entityId = 1;
        int mapId = 0;

        int halfX = mapsInX / 2;
        int halfY = mapsInY / 2;

        //Log.error("halfX=" + halfX + " halfY=" + halfY + " MAPS=" + (mapsInX * mapsInY));
        for (int x = 0; x < mapsInX; x++) {
            for (int y = 0; y < mapsInY; y++) {
                Vector3d pos = middle.add(x - halfX, -y + halfY, 0);
                createEmptyEntityMap(pos, entityId++, mapId++, list);
                //Log.error("MAP: x=" + x + " y=" + y);
            }
        }
        return list;
    }

    static void createEmptyEntityMap(Vector3d pos, int entityId, int mapId, List<PacketOut> list) {
        ItemStack item = ItemStack.builder().type(ItemTypes.FILLED_MAP).component(ComponentTypes.MAP_ID, mapId).build();

        WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ITEM_FRAME, pos, 0, 0, 0, BlockFace.SOUTH.getFaceValue(), Optional.of(Vector3d.zero()));
        //https://wiki.vg/Entity_metadata#Item_Frame
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId,
                Arrays.asList(new EntityData(8, EntityDataTypes.ITEMSTACK, item),
                        new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20)));//invisible

        list.add(new PacketPlayOutSpawnEntity(entity));
        list.add(new PacketPlayOutEntityMetadata(metadata));
        list.add(createEmptyMap(mapId));
    }

    static PacketPlayOutMap createEmptyMap(int mapId) {
        WrapperPlayServerMapData map = new WrapperPlayServerMapData(mapId, (byte) 0, false, false, null, 0, 0, 0, 0, null);
        return new PacketPlayOutMap(map);
    }

    public static void cat(ClientConnection connection) {
        */
/*int mapId = 999;
        ItemStack item = ItemStack.builder().type(ItemTypes.FILLED_MAP).component(ComponentTypes.MAP_ID, mapId).build();

        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i - 128);
        }

        PacketPlayOutMap map = createMap(mapId, bytes, 128, 2);
        WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(0, 0, 1, item);

        connection.writePacket(new PacketPlayOutSetSlot(slot));
        connection.writeAndFlushPacket(map);*//*

        new UiiaiuiiiaiCat(connection, new ArrayDeque<>(imgDeque));
    }

    private static final Deque<MovieFrame> imgDeque = new ConcurrentLinkedDeque<>();

    public static void init() {
    }

    static {
        new Thread(fromCallable(() -> {
            InputStream is = UiiaiuiiiaiCat.class.getResourceAsStream("cat_bq.mp4");
            File dataFolder = AlixCommonMain.MAIN_CLASS_INSTANCE.getDataFolderPath().toFile();

            File copy = new File(dataFolder, "cat-copy.mp4");
            copy.createNewFile();

            AlixFileManager.copy(is, copy);

            File resizedCopy = resizeVideo(copy, WIDTH, HEIGHT);
            copy.delete();

            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(resizedCopy);
            grabber.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();

            Deque<MovieFrame> deque = imgDeque; //new ConcurrentLinkedDeque<>();

            double fps = grabber.getFrameRate();
            double selectEveryXFrames = fps / MAX_FPS;

            double framesToSelect = selectEveryXFrames;

            int framesTotal = 0;
            int framesSelected = 0;
            while (framesTotal != grabber.getLengthInFrames()) {
                */
/*if (grabber.getLengthInFrames() <= grabber.getFrameNumber()) {
                    finished = true;
                }*//*


                //Log.error("framesTotal: " + framesTotal + " framesToSelect: " + framesToSelect);

                Frame frame = grabber.grab();
                framesTotal++;

                if (--framesToSelect <= 0) {
                    framesSelected++;
                    framesToSelect += selectEveryXFrames;

                    BufferedImage bufferedImage = converter.getBufferedImage(frame);
                    if (bufferedImage != null) deque.add(new MovieFrame(bufferedImage));
                }
            }
            grabber.stop();
            resizedCopy.delete();

            Log.error("FRAMES SELECTED: " + framesSelected + " TOTAL: " + framesTotal);
            return null;
        })).start();
    }

    private void cancel() {
        this.task.cancel(false);
    }

    @SneakyThrows
    @Override
    public void run() {
        MovieFrame movieFrame = this.frames.poll();
        long t0 = System.currentTimeMillis();

        if (movieFrame != null) {
            this.connection.writePackets(movieFrame.packets);
            this.connection.flush();
        } else this.cancel();

        long t1 = System.currentTimeMillis();
        long diff = t1 - t0;

        //Log.error("TOOK: " + diff + "ms");
    }

    private static int roundUp(float f) {
        return (int) (f % 1 != 0 ? f + 1 : f);
    }

    private static final class MovieFrame {

        private final List<PacketOut> packets;

        private MovieFrame(BufferedImage image) {
            this.packets = getFrame(image);
        }
    }

    private static List<PacketOut> getFrame(BufferedImage image) {
        int mapId = 0;

        int width = image.getWidth();
        int height = image.getHeight();

        int mapsInX = roundUp(width / 128f);
        int mapsInY = roundUp(height / 128f);

        List<PacketOut> packets = new ArrayList<>(mapsInX * mapsInY);

        for (int x = 0; x < mapsInX; x++) {
            for (int y = 0; y < mapsInY; y++) {

                int imageX = x * 128;
                int imageY = y * 128;

                int maxWidth = Math.min(width - imageX, 128);
                int maxHeight = Math.min(height - imageY, 128);

                BufferedImage subImage = image.getSubimage(imageX, imageY, maxWidth, maxHeight);

                packets.add(createMap(mapId++, subImage));
            }
        }
        return packets;
    }

    private static Runnable fromCallable(Callable<?> callable) {
        return () -> {
            try {
                callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @SneakyThrows
    private static File resizeVideo(File video, int width, int height) {

        // Create a FrameGrabber to read the input video
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(video);
        frameGrabber.start();

        // Calculate the aspect ratio of the original video
        double aspectRatio = (double) frameGrabber.getImageWidth() / frameGrabber.getImageHeight();

        // Calculate the new width and height based on the desired dimension and aspect ratio
        int newWidth, newHeight;
        double targetAspectRatio = (double) width / height;
        if (aspectRatio > targetAspectRatio) {
            newWidth = width;
            newHeight = (int) (width / aspectRatio);
        } else {
            newWidth = (int) (height * aspectRatio);
            newHeight = height;
        }

        // Create a FrameRecorder to write the output video
        String outputFilename = video.getAbsolutePath().substring(0, video.getAbsolutePath().lastIndexOf(".")) + "_resized.mp4";
        FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder(outputFilename, newWidth, newHeight);
        frameRecorder.setVideoCodec(frameGrabber.getVideoCodec());
        frameRecorder.setFormat("mp4");
        frameRecorder.setFrameRate(frameGrabber.getFrameRate());
        frameRecorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        frameRecorder.start();

        // Initialize OpenCVFrameConverter
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        // Process each frame
        Frame frame;
        while ((frame = frameGrabber.grab()) != null) {
            // Convert the frame to a Mat object
            Mat mat = converter.convertToMat(frame);

            // Resize the frame
            Mat resizedMat = new Mat();
            if (mat != null) {
                org.bytedeco.opencv.global.opencv_imgproc.resize(mat, resizedMat, new Size(newWidth, newHeight));
            } else {
                // Handle the case when the input frame (mat) is null
                // You can choose to skip this frame or take appropriate action
                continue; // Skip the current frame and move to the next iteration
            }

            // Convert the resized Mat back to a Frame
            Frame resizedFrame = converter.convert(resizedMat);

            // Record the resized frame
            frameRecorder.record(resizedFrame);
        }

        // Release resources
        frameGrabber.stop();
        frameRecorder.stop();

        File resizedVideo = new File(outputFilename);

        return resizedVideo;
    }
}*/
