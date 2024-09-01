package shadow.utils.misc.captcha;

import alix.common.antibot.captcha.CaptchaImageGenerator;
import alix.common.antibot.captcha.ColorGenerator;
import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleDustData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.netty.buffer.ByteBuf;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.VisibleForTesting;
import shadow.Main;
import shadow.systems.login.auth.GoogleAuth;
import shadow.systems.login.captcha.subtypes.MapCaptcha;
import shadow.utils.main.AlixUtils;
import shadow.utils.math.MathUtils;
import shadow.utils.misc.captcha.D3.ModelRenderer3d;
import shadow.utils.misc.packet.constructors.OutMapPacketConstructor;
import shadow.utils.misc.packet.custom.ParticleHashCompressor;
import shadow.utils.netty.NettyUtils;
import shadow.utils.world.AlixWorld;
import shadow.utils.world.location.ConstLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

import static java.lang.Math.*;

public final class ImageRenderer {

    private static final ConstLocation
            MODEL_3D_START = AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(2, -40, 2).toConst(),
            MODEL_CENTER = AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(0, -5, -4).toConst(),
            SMOOTH_CENTER = AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(0, -4, -3).toConst(),
            PARTICLE_CENTER = AlixWorld.TELEPORT_LOCATION.asModifiableCopy().add(0, -3, -3).toConst();
    private static final Quaternion ROTATION = new Quaternion(new Vec3f(1, 0, 0), 45);
    private static final Vector3f OFFSET = new Vector3f(0, 0, 0);
    private static final Map<Color, ItemType> COLOR_TO_MODEL_ITEM;

    public static final int ENTITY_ID_START = 100_000;//+ 1 is the actual start
    public static final int QR_ENTITY_ID_START = 1_000_000;//+ 1 is the actual start

    static {
        Map<Color, ItemType> map = new HashMap<>();
        List<Color> list = ColorGenerator.PARTICLE_COLOR_LIST;
        map.put(list.get(0), ItemTypes.BIRCH_BUTTON);
        map.put(list.get(1), ItemTypes.BIRCH_BUTTON);
        map.put(list.get(2), ItemTypes.ACACIA_BUTTON);
        map.put(list.get(3), ItemTypes.ACACIA_BUTTON);
        map.put(list.get(4), ItemTypes.OAK_BUTTON);
        map.put(list.get(5), ItemTypes.OAK_BUTTON);
        map.put(list.get(6), ItemTypes.JUNGLE_BUTTON);
        map.put(list.get(7), ItemTypes.JUNGLE_BUTTON);
        map.put(list.get(8), ItemTypes.SPRUCE_BUTTON);
        map.put(list.get(9), ItemTypes.SPRUCE_BUTTON);
        map.put(list.get(10), ItemTypes.STONE_BUTTON);
        map.put(list.get(11), ItemTypes.STONE_BUTTON);
        /*map.put(list.get(0), ItemTypes.YELLOW_BANNER);
        map.put(list.get(1), ItemTypes.GOLD_INGOT);
        map.put(list.get(2), ItemTypes.ACACIA_BUTTON);
        map.put(list.get(3), ItemTypes.ACACIA_BUTTON);
        map.put(list.get(4), ItemTypes.OAK_BUTTON);
        map.put(list.get(5), ItemTypes.OAK_BUTTON);
        map.put(list.get(6), ItemTypes.JUNGLE_BUTTON);
        map.put(list.get(7), ItemTypes.JUNGLE_BUTTON);
        map.put(list.get(8), ItemTypes.SPRUCE_BUTTON);
        map.put(list.get(9), ItemTypes.SPRUCE_BUTTON);
        map.put(list.get(10), ItemTypes.STONE_BUTTON);
        map.put(list.get(11), ItemTypes.STONE_BUTTON);*/
        COLOR_TO_MODEL_ITEM = map;
    }

    //Source code: https://github.com/whileSam/bukkit-image-renderer/blob/master/src/main/java/me/trysam/imagerenderer/particle/ImageRenderer.java

    public static ByteBuf[] recaptcha(Location loc) {
        List<ByteBuf> list = new ArrayList<>();
        int entityId = ENTITY_ID_START;

        entityId++;

        ItemStack item = ItemStack.builder().type(ItemTypes.ACACIA_BUTTON).build(); //SpigotConversionUtil.fromBukkitItemStack(AlixUtils.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGZmOTY4YzkwNTIyMzQ5MWE0ZTM5MGM3ZDVmMTBjMTg0M2E2YmEwNDgyMGQ2YjE3ODRmNTJlNDEwZDExYWI1YiJ9fX0="));

        WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND,
                SpigotConversionUtil.fromBukkitLocation(loc).getPosition(), 0, 0, 0,
                BlockFace.NORTH.getFaceValue(), Optional.of(Vector3d.zero()));
        WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment(entityId, Collections.singletonList(new Equipment(EquipmentSlot.HELMET, item)));

        //https://wiki.vg/Entity_metadata#Item_Frame
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId,
                Collections.singletonList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20)));

        list.add(NettyUtils.createBuffer(entity));
        list.add(NettyUtils.createBuffer(metadata));
        list.add(NettyUtils.createBuffer(equipment));


        //Main.logError("BUFFERS " + list.size());

        return list.toArray(new ByteBuf[0]);
    }

    public static ByteBuf[] qrCode(BufferedImage image) {
        if (image.getWidth() != image.getHeight())
            throw new AlixException("INVALID: WIDTH " + image.getWidth() + " HEIGHT " + image.getHeight());

        int side = image.getWidth();
        int entityId = QR_ENTITY_ID_START;

        int offset = (side % 128) >> 1;

        int mapsOnOneSide = (side / 128) + (offset == 0 ? 0 : 1);
        int mapsTotal = mapsOnOneSide * mapsOnOneSide;

        Map<Integer, BufferedImage> images = new HashMap(mapsTotal);

        for (int i = 0; i < mapsTotal; i++) {
            int x = i % mapsOnOneSide;
            int y = i / mapsOnOneSide;

            int xCoord = x * 128;
            int yCoord = y * 128;

            int xy = (x << 12) + y;

            //Main.logError("X COORD " + xCoord + " Y COORD " + yCoord);

            images.put(xy, image.getSubimage(xCoord, yCoord, 128, 128));
        }

        int mapId = 0;

        List<ByteBuf> list = new ArrayList<>(mapsTotal * 3);

        /*Location loc = player.getLocation();
        org.bukkit.block.BlockFace spigotFace = player.getFacing();
        BlockFace face = fromBukkitFace(player.getLocation().getYaw());*/

        for (Map.Entry<Integer, BufferedImage> entry : images.entrySet()) {
            int x = entry.getKey() >> 12;
            int y = entry.getKey() - (x << 12);
            //Main.logError("XXXX " + x + " Y " + y);

            ItemStack item = SpigotConversionUtil.fromBukkitItemStack(MapCaptcha.newCaptchaMapItem(mapId));

            entityId++;

            //loc.clone().subtract(x, y, 0).add(spigotFace.getDirection())).getPosition()
            WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ITEM_FRAME,
                    SpigotConversionUtil.fromBukkitLocation(GoogleAuth.QR_CODE_SHOW_LOC.asModifiableCopy().subtract(x, y, 0)).getPosition(), 0, 0, 0,
                    BlockFace.NORTH.getFaceValue(), Optional.of(Vector3d.zero()));
            //https://wiki.vg/Entity_metadata#Item_Frame
            WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId,
                    Collections.singletonList(new EntityData(8, EntityDataTypes.ITEMSTACK, item)));

            list.add(NettyUtils.createBuffer(entity));
            list.add(NettyUtils.createBuffer(metadata));
            list.add(OutMapPacketConstructor.constructDynamic(mapId++, CaptchaImageGenerator.imageToBytes(entry.getValue())));
        }
        //Main.logError("BUFFERS " + list.size());

        return list.toArray(new ByteBuf[0]);
    }

    //no idea whether this would even work
/*    private static BlockFace fromBukkitFace(float yaw) {
        if (yaw >= 315 || yaw <= 45) return BlockFace.NORTH;
        if (yaw >= 45 && yaw <= 135) return BlockFace.EAST;
        if (yaw >= 135 && yaw <= 215) return BlockFace.SOUTH;
        return BlockFace.WEST;
    }*/

    public static ByteBuf[] xes(BufferedImage image) {
        //The scaling factor determines how dense the particles should be together (the higher the denominator, the less the space between the particles/pixels)
        float scalingFactor = 1f / 5.5f;//było 1/8f
        List<ByteBuf> list = new ArrayList<>(1 << 11);//2048

        int entityId = ENTITY_ID_START;

        //ItemType[] types = {ItemTypes.BIRCH_BUTTON, ItemTypes.ACACIA_BUTTON, ItemTypes.OAK_BUTTON};

        //Iterates through all the pixels
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {

                //if (x % 6 == 0 || y % 6 == 0) continue;

                float px = ((float) x * scalingFactor) - ((256 * scalingFactor) / 2);
                float py = 0;
                float pz = ((float) y * scalingFactor) - ((256 * scalingFactor) / 2);

                //If there is some rotation assigned, rotate the px, py, and pz coordinates by the given quaternion.
                if (ROTATION != null) {
                    Quaternion point = new Quaternion(0, px, py, pz);
                    Quaternion rotated = ROTATION.multiplied(point).multiplied(ROTATION.getInverse());
                    px = rotated.getX();
                    py = rotated.getY();
                    pz = rotated.getZ();
                }

                //Set the renderer's location to the center + the calculated pixel coordinates.
                //TODO: Threadsafe location modification.
                Vector3d rendererLoc = new Vector3d(MODEL_CENTER.getX() + px, MODEL_CENTER.getY() + py, MODEL_CENTER.getZ() + pz);

                Color color = new Color(image.getRGB(x, y), true);

                //If there is some transparency in the pixel, go to the next pixel if there is some.
                if (color.getAlpha() < 255 || !ColorGenerator.PARTICLE_COLOR_LIST.contains(color)) continue;
                //Set the color of the particle param to the pixel color
                //ItemTypes.values().toArray(new ItemType[0])[(int) (random() * ItemTypes.values().size())]
                ItemType type = ItemTypes.GOLD_NUGGET;
                entityId++;
                //new WrapperLivingEntity(EntityTypes.ARMOR_STAND)
                WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND, rendererLoc, 0, 0, 0, 0, Optional.of(Vector3d.zero()));
                //https://wiki.vg/Entity_metadata#Armor_Stand
                //https://haselkern.com/Minecraft-ArmorStand/
                WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId,
                        Arrays.asList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                                new EntityData(15, EntityDataTypes.BYTE, (byte) 0x08),
                                new EntityData(16, EntityDataTypes.ROTATION, new Vector3f(0, 0, 0))));
                WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment(entityId, Collections.singletonList(new Equipment(EquipmentSlot.HELMET, new ItemStack.Builder().type(type).build())));

                list.add(NettyUtils.createBuffer(entity));
                list.add(NettyUtils.createBuffer(metadata));
                list.add(NettyUtils.createBuffer(equipment));
            }
        }
        return list.toArray(new ByteBuf[0]);
    }

    //
    public static ByteBuf[] model3dBuffers(Map<Vector3d, ByteBuf[]> selectablePointsToUpdateBufs) {
        List<ByteBuf> armorStands = new ArrayList<>(1 << 11);//2048
        int entityId = ENTITY_ID_START;

        List<Vector3d> vectors = ModelRenderer3d.fromBukkit(ModelRenderer3d.renderingRelativePoints(MODEL_3D_START.toVector()));

        Vector3d _vector1 = vectors.get(0);

        double minX = _vector1.getX();
        double minZ = _vector1.getZ();
        double maxX = _vector1.getX();
        double maxZ = _vector1.getZ();

        double maxY = _vector1.getY();

        for (Vector3d vector : vectors) {
            double x = vector.getX();
            double z = vector.getZ();
            double y = vector.getY();

            if (maxX < x) maxX = x;
            if (minX > x) minX = x;

            if (maxZ < z) maxZ = z;
            if (minZ > z) minZ = z;

            if (maxY < y) maxY = y;
        }

        //Vector vec = MODEL_3D_START.toVector();

        Vector3d xzCenter = new Vector3d((maxX + minX) / 2, maxY - 2, (maxZ + minZ) / 2);// new Vector3d(vec.getX(), vec.getY(), vec.getZ()); //new Vector((maxX + minX) / 2, maxY - 2, (maxZ + minZ) / 2);

        int degreeAddition = 30;
        if (360 % degreeAddition != 0)
            throw new AlixException("360 is not divisible by " + degreeAddition + "! Remainder: " + (360 % degreeAddition));


        int tpEntityId = entityId;
        System.out.println("TP ENTITY START " + tpEntityId);
        //selectablePointsToUpdateBufs.addAll(MathUtils.circlePointsAround(xzCenter, 10, degreeAddition));


        for (int degrees = 0; degrees < 360; degrees += degreeAddition) {
            List<ByteBuf> list = new ArrayList<>();
            for (Vector3d vec : vectors) {
                //for (float distance = 0; distance <= 5; distance += 0.1f) {
                double distance = Math.hypot(vec.getX() - xzCenter.getX(), vec.getZ() - xzCenter.getZ());
                Vector3d vec3d = MathUtils.pointOnACircle(xzCenter, distance, degrees); //new Vector3d(xzCenter.getX(), xzCenter.getY(), xzCenter.getZ() - distance);

                //if (degrees == 0) vectors.add(vec3d);//the initial location

                tpEntityId++;
                WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(tpEntityId, vec3d, 0, 0, false);
                list.add(NettyUtils.createBuffer(teleport));
            }
            System.out.println("TP ENTITY ID END " + tpEntityId + " BUFS: " + list.size());
            tpEntityId = ENTITY_ID_START;
            selectablePointsToUpdateBufs.put(MathUtils.pointOnACircle(xzCenter, 10, degrees), list.toArray(new ByteBuf[0]));
            list.clear();
        }
        Main.logError("POINTS: " + vectors.size());

        entityId = ENTITY_ID_START;

        int yaw = 360;
        int selectableEntityId = ENTITY_ID_START + 10000000;

        for (Vector3d rendererLoc : selectablePointsToUpdateBufs.keySet()) {
            //if (rendererLoc.getY() < maxY - 2) continue;
            selectableEntityId++;
            WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(selectableEntityId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND, rendererLoc, 0, yaw, 0, 0, Optional.of(Vector3d.zero()));
            yaw -= degreeAddition;

            //https://wiki.vg/Entity_metadata#Armor_Stand
            //https://haselkern.com/Minecraft-ArmorStand/
            WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(selectableEntityId,
                    Arrays.asList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                            new EntityData(16, EntityDataTypes.ROTATION, new Vector3f(0, 0, 0))));

            ItemStack item = SpigotConversionUtil.fromBukkitItemStack(AlixUtils.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODAwM2MyMDdjNDAzYWYwNjkzYTllMjI2MWU2ODFkNTBlYzU3Y2Y4MmJlMmY1ZDM4NmIwYjlkMjcwN2Y3MTIwOSJ9fX0="));

            WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment(selectableEntityId, Collections.singletonList(new Equipment(EquipmentSlot.HELMET, item)));

            armorStands.add(NettyUtils.createBuffer(entity));
            armorStands.add(NettyUtils.createBuffer(metadata));
            armorStands.add(NettyUtils.createBuffer(equipment));
        }

        System.out.println("NORMAL ENTITY START " + entityId);
        for (Vector3d rendererLoc : vectors) {
            //if (rendererLoc.getY() < maxY - 1) continue;
            entityId++;
            WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND, rendererLoc, 0, 0, 0, 0, Optional.of(Vector3d.zero()));
            //https://wiki.vg/Entity_metadata#Armor_Stand
            //https://haselkern.com/Minecraft-ArmorStand/
            WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId,
                    Arrays.asList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                            new EntityData(16, EntityDataTypes.ROTATION, new Vector3f(0, 0, 0))));

            ItemType type = ItemTypes.STONE_BUTTON;

            WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment(entityId, Collections.singletonList(new Equipment(EquipmentSlot.HELMET, new ItemStack.Builder().type(type).build())));

            armorStands.add(NettyUtils.createBuffer(entity));
            armorStands.add(NettyUtils.createBuffer(metadata));
            armorStands.add(NettyUtils.createBuffer(equipment));
        }
        System.out.println("NORMAL ENTITY ID END " + entityId);
        return armorStands.toArray(new ByteBuf[0]);
    }

    public static ByteBuf[] smoothModelBuffers(BufferedImage image, int imgSize) {
        //The scaling factor determines how dense the particles should be together (the higher the denominator, the less the space between the particles/pixels)
        float scalingFactor = 1f / 8f;//było 1/8f
        List<ByteBuf> list = new ArrayList<>(1 << 11);//2048

        int entityId = ENTITY_ID_START;

        //ItemType[] types = {ItemTypes.BIRCH_BUTTON, ItemTypes.ACACIA_BUTTON, ItemTypes.OAK_BUTTON};

        //Iterates through all the pixels
        for (int x = 0; x < imgSize; x++) {
            for (int y = 0; y < imgSize; y++) {

                //if (x % 6 == 0 || y % 6 == 0) continue;

                float px = ((float) x * scalingFactor) - ((imgSize * scalingFactor) / 2);
                float py = 0;
                float pz = ((float) y * scalingFactor) - ((imgSize * scalingFactor) / 2);

                //If there is some rotation assigned, rotate the px, py, and pz coordinates by the given quaternion.
                if (ROTATION != null) {
                    Quaternion point = new Quaternion(0, px, py, pz);
                    Quaternion rotated = ROTATION.multiplied(point).multiplied(ROTATION.getInverse());
                    px = rotated.getX();
                    py = rotated.getY();
                    pz = rotated.getZ();
                }

                //Set the renderer's location to the center + the calculated pixel coordinates.
                Vector3d rendererLoc = new Vector3d(SMOOTH_CENTER.getX() + px, SMOOTH_CENTER.getY() + py, SMOOTH_CENTER.getZ() + pz);

                Color color = new Color(image.getRGB(x, y), true);

                //If there is some transparency in the pixel, go to the next pixel if there is some.
                if (color.getAlpha() < 255 || !ColorGenerator.PARTICLE_COLOR_LIST.contains(color)) continue;
                //Set the color of the particle param to the pixel color
                //ItemTypes.values().toArray(new ItemType[0])[(int) (random() * ItemTypes.values().size())]
                ItemType type = COLOR_TO_MODEL_ITEM.getOrDefault(color, ItemTypes.RED_BANNER);
                ItemStack HEAD_ITEM = new ItemStack.Builder().type(type).build();
                entityId++;
                //WrapperPlayServerSpawnEntity
                //WrapperPlayServerSpawnLivingEntity entity = new WrapperPlayServerSpawnLivingEntity(entityId, UUID.randomUUID(), EntityTypes.ARMOR_STAND, rendererLoc, 0, 0, 0, Vector3d.zero(), Arrays.asList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                //        new EntityData(16, EntityDataTypes.ROTATION, new Vector3f(45, 0, 0))));
                //WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment(entityId, Collections.singletonList(new Equipment(EquipmentSlot.HELMET, HEAD_ITEM)));

                WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND, rendererLoc, 45, 0, 0, 0, Optional.of(Vector3d.zero()));
                //https://wiki.vg/Entity_metadata#Armor_Stand
                //https://haselkern.com/Minecraft-ArmorStand/
                WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId,
                        Arrays.asList(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                                new EntityData(16, EntityDataTypes.ROTATION, new Vector3f(45, 0, 0))));
                WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment(entityId, Collections.singletonList(new Equipment(EquipmentSlot.HELMET, HEAD_ITEM)));

                list.add(NettyUtils.createBuffer(entity));
                list.add(NettyUtils.createBuffer(metadata));
                list.add(NettyUtils.createBuffer(equipment));
            }
        }
        return list.toArray(new ByteBuf[0]);
    }

    public static ByteBuf[] particleBuffers(BufferedImage image) {
        //The scaling factor determines how dense the particles should be together (the higher the denominator, the less the space between the particles/pixels)
        float scalingFactor = 1f / 5.5f;//było 1/8f
        boolean far = false;
        ParticleHashCompressor compressor = new ParticleHashCompressor();

        //Iterates through all the pixels
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {

                //if (x % 6 == 0 || y % 6 == 0) continue;

                float px = ((float) x * scalingFactor) - ((128 * scalingFactor) / 2);
                float py = 0;
                float pz = ((float) y * scalingFactor) - ((128 * scalingFactor) / 2);

                //If there is some rotation assigned, rotate the px, py, and pz coordinates by the given quaternion.
                if (ROTATION != null) {
                    Quaternion point = new Quaternion(0, px, py, pz);
                    Quaternion rotated = ROTATION.multiplied(point).multiplied(ROTATION.getInverse());
                    px = rotated.getX();
                    py = rotated.getY();
                    pz = rotated.getZ();
                }

                //Set the renderer's location to the center + the calculated pixel coordinates.
                //TODO: Threadsafe location modification.
                Vector3d rendererLoc = new Vector3d(PARTICLE_CENTER.getX() + px, PARTICLE_CENTER.getY() + py, PARTICLE_CENTER.getZ() + pz);

                Color color = new Color(image.getRGB(x, y), true);

                //If there is some transparency in the pixel, go to the next pixel if there is some.
                if (color.getAlpha() < 255 || !ColorGenerator.PARTICLE_COLOR_LIST.contains(color)) continue;
                //Set the color of the particle param to the pixel color

                Particle particle = new Particle(ParticleTypes.DUST, new ParticleDustData(1.5f, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f));
                WrapperPlayServerParticle wrapper = new WrapperPlayServerParticle(particle, far, rendererLoc, OFFSET, 0f, 0);
                compressor.tryAdd(wrapper);
            }
        }
        return compressor.buffers();
    }

    @VisibleForTesting
    public static ByteBuf[] list(BufferedImage image) {
        //The scaling factor determines how dense the particles should be together (the higher the denominator, the less the space between the particles/pixels)
        float scalingFactor = 1f / 8f;
        boolean far = false;
        List<ByteBuf> list = new ArrayList<>(1024);

        //Iterates through all the pixels
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {

                float px = ((float) x * scalingFactor) - ((128 * scalingFactor) / 2);
                float py = 0;
                float pz = ((float) y * scalingFactor) - ((128 * scalingFactor) / 2);

                //If there is some rotation assigned, rotate the px, py, and pz coordinates by the given quaternion.
                if (ROTATION != null) {
                    Quaternion point = new Quaternion(0, px, py, pz);
                    Quaternion rotated = ROTATION.multiplied(point).multiplied(ROTATION.getInverse());
                    px = rotated.getX();
                    py = rotated.getY();
                    pz = rotated.getZ();
                }

                //Set the renderer's location to the center + the calculated pixel coordinates.
                //TODO: Threadsafe location modification.
                Vector3d rendererLoc = new Vector3d(PARTICLE_CENTER.getX() + px, PARTICLE_CENTER.getY() + py, PARTICLE_CENTER.getZ() + pz);

                Color color = new Color(image.getRGB(x, y), true);

                //If there is some transparency in the pixel, go to the next pixel if there is some.
                if (color.getAlpha() < 255 || !ColorGenerator.PARTICLE_COLOR_LIST.contains(color)) continue;

                //Set the color of the particle param to the pixel color

                Particle particle = new Particle(ParticleTypes.DUST, new ParticleDustData(1.5f, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f));
                WrapperPlayServerParticle wrapper = new WrapperPlayServerParticle(particle, far, rendererLoc, OFFSET, 0f, 0);
                list.add(NettyUtils.createBuffer(wrapper));
            }
        }
        return list.toArray(new ByteBuf[0]);
    }

    private static final class Vec3f {

        private final float x;
        private final float y;
        private final float z;

        private Vec3f(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param number The number to be multiplied with.
         * @return The product of this vector and the given parameter.
         */
        private Vec3f multiplied(float number) {
            return new Vec3f(x * number, y * number, z * number);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The unit vector of this vector. The length of the new vector is 1.
         */
        private Vec3f normalized() {
            float length = getLength();
            return new Vec3f(x / length, y / length, z / length);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The dot product of this vector and the given parameter.
         */
        private float dot(Vec3f vec) {
            return x * vec.x + y * vec.y + z * vec.z;
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The cross product of this vector and the given parameter.
         */
        private Vec3f cross(Vec3f vec) {
            float newX = y * vec.z - z * vec.y;
            float newY = z * vec.x - x * vec.z;
            float newZ = x * vec.y - y * vec.x;
            return new Vec3f(newX, newY, newZ);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The sum of this vector and the given parameter.
         */
        private Vec3f added(Vec3f vec) {
            return new Vec3f(x + vec.x, y + vec.y, z + vec.z);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The length of this vector.
         */
        private float getLength() {
            return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        }

        private float getX() {
            return x;
        }

        private float getY() {
            return y;
        }

        private float getZ() {
            return z;
        }
    }

    private static final class Vec3d {

        double x;
        double y;
        double z;

        private Vec3d(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }


        private static Vec3d fromLocation(Location location) {
            return new Vec3d(location.getX(), location.getY(), location.getZ());
        }


        private static Vec3d fromBukkitVector(Vector vector) {
            return new Vec3d(vector.getX(), vector.getY(), vector.getZ());
        }

        private double distanceTo(Vec3d vec) {
            return Math.sqrt(Math.pow(x - vec.x, 2) + Math.pow(y - vec.y, 2) + Math.pow(z - vec.z, 2));
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param number The number to be multiplied with.
         * @return The product of this vector and the given parameter.
         */
        private Vec3d multiplied(float number) {
            return new Vec3d(x * number, y * number, z * number);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The unit vector of this vector. The length of the new vector is 1.
         */
        private Vec3d normalized() {
            double length = getLength();
            return new Vec3d(x / length, y / length, z / length);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The dot product of this vector and the given parameter.
         */
        private double dot(Vec3d vec) {
            return x * vec.x + y * vec.y + z * vec.z;
        }


        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The cross product of this vector and the given parameter.
         */
        private Vec3d cross(Vec3d vec) {
            double newX = y * vec.z - z * vec.y;
            double newY = z * vec.x - x * vec.z;
            double newZ = x * vec.y - y * vec.x;
            return new Vec3d(newX, newY, newZ);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @param vec The other vector to be calculated with.
         * @return The sum of this vector and the given parameter.
         */
        private Vec3d added(Vec3d vec) {
            return new Vec3d(x + vec.x, y + vec.y, z + vec.z);
        }

        /**
         * Doesn't change anything in this instance.
         *
         * @return The length of this vector.
         */
        private double getLength() {
            return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        }

        private void setX(double x) {
            this.x = x;
        }

        private void setY(double y) {
            this.y = y;
        }

        private void setZ(double z) {
            this.z = z;
        }

        private double getX() {
            return x;
        }

        private double getY() {
            return y;
        }

        private double getZ() {
            return z;
        }
    }

    private static final class Quaternion {

        private final Vec3f n;
        private final float w;
        private final float x;
        private final float y;
        private final float z;

        /**
         * Used to create a quaternion representing a certain axis in a 3-dimensional, cartesian coordinate system.
         *
         * @param n             The rotation axis
         * @param theta_degrees Rotation in degrees.
         */
        private Quaternion(Vec3f n, double theta_degrees) {
            Vec3f n2 = n.normalized();
            float theta = (float) toRadians(theta_degrees);
            Vec3f n_multiplied = n2.multiplied((float) sin(theta / 2));
            w = (float) cos(theta / 2);
            x = n_multiplied.getX();
            y = n_multiplied.getY();
            z = n_multiplied.getZ();
            this.n = new Vec3f(x, y, z);
        }

        /**
         * Used to create a point in 3-dimensional space.
         *
         * @param w The angle of the quaternion (needs to be the cosine of the half of an angle in radians). If used to create a point, set this to 0.
         * @param x The x variable of the quaternion.
         * @param y The y variable of the quaternion.
         * @param z The z variable of the quaternion.
         */
        private Quaternion(float w, float x, float y, float z) {
            n = new Vec3f(x, y, z);
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Doesn't change anything to this quaternion.
         *
         * @return The inverse of the current quaternion.
         */
        private Quaternion getInverse() {
            Vec3f inverse = n.multiplied(-1);
            return new Quaternion(w, inverse.getX(), inverse.getY(), inverse.getZ());
        }

        /**
         * Doesn't change anything to this quaternion.
         *
         * @param quat The other quaternion to be calculated with.
         * @return The quaternion product of this quaternion and the given parameter
         */
        private Quaternion multiplied(Quaternion quat) {
            float newW = w * quat.w - n.dot(quat.n);
            Vec3f newN = n.multiplied(quat.w).added(quat.n.multiplied(w)).added(n.cross(quat.n));
            return new Quaternion(newW, newN.getX(), newN.getY(), newN.getZ());
        }


        private Vec3f getN() {
            return n;
        }

        private float getW() {
            return w;
        }

        private float getX() {
            return x;
        }

        private float getY() {
            return y;
        }

        private float getZ() {
            return z;
        }
    }
}