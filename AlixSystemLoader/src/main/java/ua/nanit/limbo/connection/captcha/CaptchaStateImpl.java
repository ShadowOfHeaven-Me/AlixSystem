package ua.nanit.limbo.connection.captcha;

import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import io.netty.util.concurrent.ScheduledFuture;
import ua.nanit.limbo.NanoLimbo;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.connection.captcha.arm.ArmAnimations;
import ua.nanit.limbo.connection.captcha.held.HeldItemSlots;
import ua.nanit.limbo.connection.pipeline.PacketDuplexHandler;
import ua.nanit.limbo.protocol.PacketOut;
import ua.nanit.limbo.protocol.PacketSnapshot;
import ua.nanit.limbo.protocol.PacketSnapshots;
import ua.nanit.limbo.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import ua.nanit.limbo.protocol.registry.Version;
import ua.nanit.limbo.server.Log;

import java.util.concurrent.TimeUnit;

import static ua.nanit.limbo.protocol.PacketSnapshots.TELEPORT_VALID_Y;
import static ua.nanit.limbo.protocol.PacketSnapshots.TELEPORT_Y;

final class CaptchaStateImpl {

    private static final PacketSnapshot TIMED_OUT = PacketPlayOutDisconnect.error("Timed out");
    /*
    private static final PacketSnapshot INVALID_ID = PacketPlayOutDisconnect.error("Invalid id");
    private static final PacketSnapshot INVALID_PONG_ID = PacketPlayOutDisconnect.error("Invalid pong id");
    private static final PacketSnapshot INVALID_GROUND = PacketPlayOutDisconnect.error("Invalid ground");
    private static final PacketSnapshot NO_PONG = PacketPlayOutDisconnect.error("No pong");
    private static final PacketSnapshot NO_KEEP_ALIVE = PacketPlayOutDisconnect.error("No keep alive");
    private static final PacketSnapshot INVALID_XZ = PacketPlayOutDisconnect.error("Invalid XZ");
    private static final PacketSnapshot INVALID_Y = PacketPlayOutDisconnect.error("Invalid Y");
    private static final PacketSnapshot INVALID_Y_COL = PacketPlayOutDisconnect.error("Invalid Y Col");
    private static final PacketSnapshot INVALID_DELTA_Y = PacketPlayOutDisconnect.error("Invalid Delta Y");
    private static final PacketSnapshot INVALID_POS_CHANGE = PacketPlayOutDisconnect.error("Invalid Pos Change");
    private static final PacketSnapshot INVALID_MOVE = PacketPlayOutDisconnect.error("Invalid Move");
    //private static final PacketSnapshot INVALID_TRANSACTION = PacketPlayOutDisconnect.error("Invalid Transaction");
    private static final PacketSnapshot INVALID_HELD = PacketPlayOutDisconnect.error("Invalid Held");
    private static final PacketSnapshot INVALID_HAND = PacketPlayOutDisconnect.error("Invalid Hand");*/

    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;

    private ScheduledFuture<?> disconnectTask;
    private long keepAliveSentTime;
    private byte movementsReceived, keepAlivesReceived;
    private boolean movementsSent;

    //Location
    private double x, y, z;
    private float yaw, pitch;

    CaptchaStateImpl(ClientConnection connection) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        connection.getChannel().closeFuture().addListener(f -> this.cancelTask());
        //this.expectedId = 1;
    }

    private boolean isAwaitingTransaction, hasReceivedTransaction;

    void sendInitial0() {
        this.write(PacketSnapshots.PLAYER_ABILITIES_FALL);
        if (this.version().moreOrEqual(Version.V1_20_5)) {
            this.write(Cookies.COOKIE_STORE_EMPTY);
            this.write(Cookies.COOKIE_REQ_EMPTY);
            this.write(Cookies.COOKIE_REQ_NULL);
            this.cookieResponse |= IS_AWAITING_COOKIE_RESPONSE;
        }

        if (this.version().moreOrEqual(Version.V1_17)) {
            this.write(PacketPings.INITIAL_PING);
            this.isAwaitingTransaction = true;
        } else if (this.version().moreOrEqual(Version.V1_8)) {
            this.write(Transactions.VALID);
            this.isAwaitingTransaction = true;
        }
        this.write(KeepAlives.INITIAL_KEEP_ALIVE);
        if (this.version().moreOrEqual(Version.V1_20_2)) {
            //this.write(ChunkBatches.BATCH_START);
            this.write(ChunkBatches.BATCH_END);
            this.isAwaitingBatchAck = true;
        }

        this.writeAndFlush(KeepAlives.SECONDARY_KEEP_ALIVE);

        this.keepAliveSentTime = System.currentTimeMillis();
        this.lastKeepAliveSentTime = this.keepAliveSentTime;
        this.scheduleKickTask();
    }

    private static final byte
            IS_AWAITING_COOKIE_RESPONSE = 1,
            COOKIE_EMPTY_RESPONSE_RECEIVED = 1 << 1,
            COOKIE_NULL_RESPONSE_RECEIVED = 1 << 2,
            BOTH_RESPONSES_RECEIVED = COOKIE_EMPTY_RESPONSE_RECEIVED | COOKIE_NULL_RESPONSE_RECEIVED;
    private byte cookieResponse;

    private boolean isAwaitingCookieResponse() {
        return (this.cookieResponse & IS_AWAITING_COOKIE_RESPONSE) != 0;
    }

    private boolean hasReceivedBothCookieResponses() {
        return (this.cookieResponse & BOTH_RESPONSES_RECEIVED) == BOTH_RESPONSES_RECEIVED;
    }

    void handle(WrapperPlayClientCookieResponse packet) {
        String key = packet.getKey().toString();
        byte[] payload = packet.getPayload();

        if (key.equals(Cookies.KEY_EMPTY)) {
            this.failIf((this.cookieResponse & COOKIE_NULL_RESPONSE_RECEIVED) != 0);
            this.failIf(payload == null || payload.length != 0);
            this.cookieResponse |= COOKIE_EMPTY_RESPONSE_RECEIVED;
            return;
        }

        if (key.equals(Cookies.KEY_NULL)) {
            this.failIf((this.cookieResponse & COOKIE_EMPTY_RESPONSE_RECEIVED) == 0);
            this.failIf(payload != null);
            this.cookieResponse |= COOKIE_NULL_RESPONSE_RECEIVED;
        }
    }

    private boolean isAwaitingBatchAck, hasReceivedBatchAck;

    void handle(WrapperPlayClientChunkBatchAck packet) {
        //Log.error("RECEIVED CHUNK BATCH: " + packet.getDesiredChunksPerTick());
        this.hasReceivedBatchAck = true;
    }

    private byte tpConfirmsReceived;

    void handle(WrapperPlayClientTeleportConfirm tpConfirm) {
        //Log.error("KEEP ALIVES RECEIVED: " + this.keepAlivesReceived);
        this.failIf(this.keepAlivesReceived != 2);

        //Log.error("RECEIVED TP CONFIRM ID: " + tpConfirm.getTeleportId());
        this.tpConfirmsReceived++;
        switch (tpConfirm.getTeleportId()) {
            case 1:
                this.failIf(this.movementsReceived != 0 || this.tpConfirmsReceived != 1);
                return;
            case 2:
                this.failIf(this.movementsReceived != 1 || this.tpConfirmsReceived != 2);
                return;
            default: {
                this.fail();
            }
        }
    }

    void handle(WrapperPlayClientWindowConfirmation packet) {
        this.handleTransaction0(packet.getActionId());
    }

    void handle(WrapperPlayClientPong ping) {
        this.handleTransaction0(ping.getId());
    }

    private static final int BLOCK_Y = 60;
    static final Vector3i BLOCK_POS = new Vector3i(0, BLOCK_Y, 0);

    private void handleTransaction0(int id) {
        this.failIf(id != 1);
        this.failIf(this.keepAlivesReceived != 2);

        this.failIf(this.isAwaitingCookieResponse() && !this.hasReceivedBothCookieResponses());

        this.hasReceivedTransaction = true;
        /*int blockY = 60;//AlixMathUtils.roundUp(predictedNextY);
        Vector3i pos = new Vector3i(0, blockY, 0);*/

        /*this.connection.getChannel().eventLoop().schedule(() -> {
            CaptchaBlockCheck.sendRandomBlock(this.connection, pos);
            Log.error("SENT BLOCKCCCCCCC (ONCE AGAIN)");
        }, 3, TimeUnit.SECONDS);*/
        CaptchaBlock block = CaptchaBlock.sendRandomBlock(this.connection);

        this.expectedYCollision = BLOCK_Y + block.getHeight(this.version());
    }

    private void cancelTask() {
        if (this.disconnectTask != null) this.disconnectTask.cancel(false);
    }

    private void scheduleDisconnectTask(long delay, TimeUnit unit) {
        if (NanoLimbo.performChecks) return;

        this.disconnectTask = this.connection.getChannel().eventLoop().schedule(() -> this.disconnect(TIMED_OUT), delay, unit);
    }

    long sent;

    void handle(WrapperPlayClientKeepAlive keepAlive) {
        //EpollSocketChannel c

        //int id = (int) keepAlive.getId();
        //if (id != keepAlive.getId()) {//Below 1.12.2 are ints, above they never sent keep alives by themselves
        long id = keepAlive.getId();
        //Log.error("RECEIVED KEEP ALIVE ID: " + keepAlive.getId());
        if (movementsSent) {
            this.failIf(id != KeepAlives.PREVENT_TIMEOUT_ID);
            return;
        }

        this.keepAlivesReceived++;

        switch (this.keepAlivesReceived) {
            case 1:
                this.failIf(id != KeepAlives.INITIAL_ID);
                return;
            case 2:
                break;
            default:
                this.fail();
                return;
        }

        /*if (id != 1 && !this.pingReceived) {
            //this.close(NO_PONG);
            //return;
        }*/

        this.failIf(this.isAwaitingBatchAck && !this.hasReceivedBatchAck);
        this.failIf(id != KeepAlives.SECONDARY_ID);

        this.cancelTask();
        long delay = System.currentTimeMillis() - this.keepAliveSentTime;//the delay for both ways in millis
        //max wait time - network two-way delay + 150 millis for client ticks + 200 millis in back-up
        long maxWaitTime = Math.min(delay + 350, 4000);
        this.scheduleDisconnectTask(maxWaitTime, TimeUnit.MILLISECONDS);
        this.sent = System.currentTimeMillis();
        //Log.error("delay: " + delay + " maxWaitTime: " + maxWaitTime);

        /*if (id == 5) {
            //Log.error("KEEP ALIVE MOVEMENTS RECEIVED: " + this.movementsReceived);
            this.disconnectTask = null;
            return;
        }

        this.duplexHandler.writeAndFlushPacket(KeepAlives.KEEP_ALIVES[id]);
        this.expectedId = id + 1;
        this.disconnectTask = this.connection.getChannel().eventLoop().schedule(() -> this.close(TIMED_OUT), 3, TimeUnit.SECONDS);*/
    }

    private boolean awaitingArmAnimation;

    void handle(WrapperPlayClientAnimation packet) {
        if (!this.awaitingArmAnimation) return;

        this.failIf(packet.getHand() != InteractionHand.MAIN_HAND);

        this.cancelTask();
        this.awaitingArmAnimation = false;
        ////Log.error("ALL CHECKS PASSED IN: " + (System.currentTimeMillis() - this.keepAliveSentTime));
        this.connection.verify();
    }

    /*//https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/verification/FallbackProtocolHandler.java#L49
    void handle(PacketPlayInTransaction packet) {
        WrapperPlayClientWindowConfirmation transaction = packet.wrapper();
        //https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/protocol/packets/play/TransactionPacket.java#L45
        boolean accepted = transaction.isAccepted() || this.connection.getClientVersion().moreOrEqual(Version.V1_17);
        if (!this.isAwaitingTransaction || !accepted || transaction.getWindowId() != 0
                || transaction.getActionId() != 0) {//we sent 0 as the transaction id
            this.disconnect(INVALID_TRANSACTION);
            return;
        }
        this.cancelTask()

        //https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/verification/FallbackProtocolHandler.java#L146

        //should be ignored by the client
        this.write(HeldItemSlots.INVALID);
        //the duplicate should be ignored
        this.write(HeldItemSlots.VALID);
        this.writeAndFlush(HeldItemSlots.VALID);

        this.scheduleDisconnectTask(5, TimeUnit.SECONDS);
        this.awaitingHeldSlot = true;
    }*/

    private boolean awaitingHeldSlot;
    private int currentSlot = -1;

    void handle(WrapperPlayClientHeldItemChange packet) {
        int slot = packet.getSlot();
        this.failIf(slot == this.currentSlot//the player cannot send duplicate item selects
                || slot < 0 || slot > 8);
        this.currentSlot = slot;

        if (this.awaitingHeldSlot && slot == HeldItemSlots.VALID_SLOT) {
            this.cancelTask();
            this.awaitingHeldSlot = false;

            this.scheduleKickTask();
            this.writeAndFlush(ArmAnimations.SELF_SWING);
            this.awaitingArmAnimation = true;
        }
    }

    private long lastKeepAliveSentTime;
    private double deltaY, lastDeltaY, expectedYCollision;
    private boolean isFalling, isCheckingCollision, checkedCollision;

    void handle(WrapperPlayClientPlayerFlying flying) {
        Location loc = flying.getLocation();

        if (flying.hasRotationChanged()) {
            this.yaw = loc.getYaw();
            this.pitch = loc.getPitch();
        }

        if (flying.hasPositionChanged()) {

            double lastY = this.y;

            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();

            this.lastDeltaY = deltaY;
            this.deltaY = this.y - lastY;
        }

        this.failIf(flying.isOnGround() && !this.checkedCollision && (!this.isCheckingCollision || this.y != this.expectedYCollision));
        ////Log.error("checkedCollision " + checkedCollision + " isCheckingCollision " + isCheckingCollision + " y " + y + " expectedYCollision " + expectedYCollision);


        //the player does not have the ability to move on X, Z
        //and is spawned on X = Z = VALID_XZ
        this.failIf(!this.checkedCollision && (this.x != PacketSnapshots.VALID_XZ || this.z != PacketSnapshots.VALID_XZ));

        this.failIf(!flying.hasPositionChanged() && this.isFalling/*|| flying.hasPositionChanged() && !this.isFalling*/);

        //we received 4 or more (this packet included)
        if (this.movementsReceived >= 3 && !flying.isOnGround()) {
            double predictedDeltaY = (this.lastDeltaY - 0.08) * 0.98;
            double diff = this.deltaY - predictedDeltaY;
            ////Log.error("DELTA Y: %s PREDICTED: %s DIFF: %s", deltaY, predictedDeltaY, diff);

            //a fair maximum deviation
            //todo
            this.failIf(Math.abs(diff) > 1E-5);
        }

        if (this.isCheckingCollision) {
            //Log.error("COLLISION ON Y: " + this.y + " EXPECTED: " + this.expectedYCollision + " ground: " + flying.isOnGround());
            //todo
            this.failIf(this.y < this.expectedYCollision);

            if (flying.isOnGround()) {
                this.failIf(this.expectedYCollision != this.y);
                ////Log.error("COLLISION ON Y: " + this.y + " EXPECTED: " + this.expectedYCollision);
                this.isCheckingCollision = false;
                this.checkedCollision = true;

                //this.writeAndFlush(new PacketPlayOutTransaction().setTransactionId(0));
                //this.isAwaitingTransaction = true;

                //https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/fallback/verification/FallbackProtocolHandler.java#L146

                //should be ignored by the client
                this.write(HeldItemSlots.INVALID);
                //should be responded to
                this.write(HeldItemSlots.VALID);
                //the duplicate should be ignored by the client
                this.writeAndFlush(HeldItemSlots.VALID);

                this.scheduleKickTask();
                this.awaitingHeldSlot = true;

                //Log.error("COLLISION ON Y: " + this.y + " EXPECTED: " + this.expectedYCollision);
            }
        }

        if (NanoLimbo.logPos) Log.error("LOC: X: %s Y: %s Z: %s YAW: %s PITCH: %s", x, y, z, yaw, pitch);

        if (this.movementsSent) {
            long now = System.currentTimeMillis();
            long lastKeepAliveSent = now - lastKeepAliveSentTime;

            if (lastKeepAliveSent >= 12000) {
                this.writeAndFlush(KeepAlives.KEEP_ALIVE_PREVENT_TIMEOUT);
                this.lastKeepAliveSentTime = now;
            }
            return;
        }

        this.movementsReceived++;
        switch (this.movementsReceived) {
            case 1: {
                this.failIf(this.tpConfirmsReceived != 1);
                ////Log.error("Y: " + this.y + " TELEPORT_Y: " + TELEPORT_Y + " EQUAL: " + (TELEPORT_Y == this.y));
                this.failIf((int) this.y != TELEPORT_Y);
                this.failIf(!flying.hasRotationChanged());
                return;
            }
            case 2: {
                this.failIf(this.tpConfirmsReceived != 2);
                ////Log.error("Y: " + this.y + " TELEPORT_Y: " + TELEPORT_Y + " EQUAL: " + (TELEPORT_VALID_Y == this.y));
                this.failIf((int) this.y != TELEPORT_VALID_Y);
                this.failIf(this.keepAlivesReceived != 2);
                this.failIf(!flying.hasRotationChanged());
                //this.duplexHandler.writePacket(PacketSnapshots.PACKET_PLAYER_POS_AND_LOOK_VALID);

                //long d = System.currentTimeMillis() - sent;

                //Log.error("RECEIVED MOVES IN: " + d + "ms");
                this.cancelTask();
                return;
            }
            case 3: {
                //same as the second movement, the client will start falling only after sending this packet
                this.failIf((int) this.y != TELEPORT_VALID_Y);
                this.failIf(this.isAwaitingTransaction && !this.hasReceivedTransaction);
                this.isFalling = true;
                return;
            }
            case 10: {
                this.movementsSent = true;
                this.isFalling = false;
                this.isCheckingCollision = true;

                //double predictedDeltaY = (this.deltaY - 0.08) * 0.98;
                //double predictedNextY = this.y + predictedDeltaY;


                //this.duplexHandler.writeAndFlushPacket(PLAYER_ABILITIES_FLY);


                //SpinningDonut.spinningDonut(this.connection);
                //long d = System.currentTimeMillis() - sent;

                ////Log.error("ALL 10 MOVES IN: " + d + "ms");
            }
        }
        //this.duplexHandler.writeAndFlushPacket(new PacketPlayOutPing().setId(ThreadLocalRandom.current().nextInt()));
        //this.duplexHandler.writeAndFlushPacket(new PacketPlayOutKeepAlive().setId(ThreadLocalRandom.current().nextInt()));
    }

    private void scheduleKickTask() {
        this.scheduleDisconnectTask(4, TimeUnit.SECONDS);
    }

    private void failIf(boolean flag) {
        if (flag) this.fail();
    }

    private void fail() {
        //Log.error();
        //Thread.currentThread().getStackTrace();
        //Log.error("FAILED THE-");
        //new Exception().printStackTrace();
        if (!NanoLimbo.performChecks) throw CaptchaFailedException.FAILED;
    }

    void disconnect(PacketOut disconnectPacket) {
        this.connection.sendPacketAndClose(disconnectPacket);
    }

    private void write(PacketOut packet) {
        this.duplexHandler.write(packet);
    }

    private void writeAndFlush(PacketOut packet) {
        this.duplexHandler.writeAndFlush(packet);
    }

    private Version version() {
        return this.connection.getClientVersion();
    }
}