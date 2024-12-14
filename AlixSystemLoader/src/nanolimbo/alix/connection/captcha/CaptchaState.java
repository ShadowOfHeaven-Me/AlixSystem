package nanolimbo.alix.connection.captcha;

import alix.libs.com.github.retrooper.packetevents.protocol.world.Location;
import alix.libs.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import io.netty.util.concurrent.ScheduledFuture;
import nanolimbo.alix.connection.ClientConnection;
import nanolimbo.alix.connection.pipeline.PacketDuplexHandler;
import nanolimbo.alix.protocol.PacketSnapshot;
import nanolimbo.alix.protocol.packets.play.disconnect.PacketPlayOutDisconnect;
import nanolimbo.alix.protocol.packets.play.keepalive.PacketInPlayKeepAlive;
import nanolimbo.alix.protocol.packets.play.move.FlyingPacket;
import nanolimbo.alix.protocol.packets.play.ping.PacketPlayInPong;
import nanolimbo.alix.protocol.packets.play.teleport.PacketPlayInTeleportConfirm;
import nanolimbo.alix.protocol.packets.play.transaction.PacketPlayInTransaction;
import nanolimbo.alix.server.Log;

import java.util.concurrent.TimeUnit;

import static nanolimbo.alix.protocol.PacketSnapshots.*;

public final class CaptchaState {

    private static final PacketSnapshot TIMED_OUT = PacketPlayOutDisconnect.error("Timed out");
    private static final PacketSnapshot INVALID_ID = PacketPlayOutDisconnect.error("Invalid id");
    private static final PacketSnapshot INVALID_ID2 = PacketPlayOutDisconnect.error("Invalid id[2]");
    private static final PacketSnapshot INVALID_PONG_ID = PacketPlayOutDisconnect.error("Invalid pong id");
    private static final PacketSnapshot INVALID_GROUND = PacketPlayOutDisconnect.error("Invalid ground");
    private static final PacketSnapshot NO_PONG = PacketPlayOutDisconnect.error("No pong");
    private static final PacketSnapshot NO_KEEP_ALIVE = PacketPlayOutDisconnect.error("No keep alive");
    private static final PacketSnapshot INVALID_XZ = PacketPlayOutDisconnect.error("Invalid XZ");
    private static final PacketSnapshot INVALID_Y = PacketPlayOutDisconnect.error("Invalid Y");
    private static final PacketSnapshot INVALID_DELTA_Y = PacketPlayOutDisconnect.error("Invalid Delta Y");
    private static final PacketSnapshot INVALID_POS_CHANGE = PacketPlayOutDisconnect.error("Invalid Pos Change");
    private static final PacketSnapshot INVALID_MOVE = PacketPlayOutDisconnect.error("Invalid Move");

    private final ClientConnection connection;
    private final PacketDuplexHandler duplexHandler;

    private ScheduledFuture<?> disconnectTask;
    private long keepAliveSentTime;
    private byte movementsReceived;
    private boolean movementsSent, pongReceived, keepAliveReceived;

    //Location
    private double x, y, z;
    private float yaw, pitch;

    public CaptchaState(ClientConnection connection) {
        this.connection = connection;
        this.duplexHandler = connection.getDuplexHandler();
        //this.expectedId = 1;
    }

    public void sendInitialKeepAlive() {
        this.duplexHandler.writeAndFlushPacket(KeepAlives.INITIAL_KEEP_ALIVE);
        this.keepAliveSentTime = System.currentTimeMillis();
        this.lastKeepAliveSentTime = this.keepAliveSentTime;
        this.scheduleDisconnectTask(3, TimeUnit.SECONDS);
    }

    private byte tpConfirmsReceived;

    public void handle(PacketPlayInTeleportConfirm tpConfirm) {
        Log.error("RECEIVED TP CONFIRM ID: " + tpConfirm.wrapper().getTeleportId());
        this.tpConfirmsReceived++;
        switch (tpConfirm.wrapper().getTeleportId()) {
            case 1:
                if (this.movementsReceived != 0 || this.tpConfirmsReceived != 1) {
                    this.connection.sendPacketAndClose(INVALID_MOVE);
                    return;
                }
                return;
            case 2:
                if (this.movementsReceived != 1 || this.tpConfirmsReceived != 2) {
                    this.connection.sendPacketAndClose(INVALID_MOVE);
                    return;
                }
                return;
            default: {
                this.connection.sendPacketAndClose(INVALID_MOVE);
            }
        }
    }

    public void handle(PacketPlayInPong ping) {
        if (ping.wrapper().getId() != 1) {
            this.connection.sendPacketAndClose(INVALID_PONG_ID);
            return;
        }
        this.pongReceived = true;
        Log.error("RECEIVED PONG");
    }

    private void scheduleDisconnectTask(long delay, TimeUnit unit) {
        this.disconnectTask = connection.getChannel().eventLoop().schedule(() -> this.connection.sendPacketAndClose(TIMED_OUT), delay, unit);
    }

    long sent;

    public void handle(PacketInPlayKeepAlive keepAlive) {
        //int id = (int) keepAlive.getId();
        //if (id != keepAlive.getId()) {//Below 1.12.2 are ints, above they never sent keep alives by themselves
        if (movementsSent) {
            if (keepAlive.getId() != KeepAlives.PREVENT_TIMEOUT_ID) {
                this.connection.sendPacketAndClose(INVALID_ID);
                return;
            }
            return;
        }

        if (this.keepAliveReceived) {
            this.connection.sendPacketAndClose(INVALID_ID2);
            return;
        }

        if (keepAlive.getId() != KeepAlives.INITIAL_ID) {
            this.connection.sendPacketAndClose(INVALID_ID);
            return;
        }

        /*if (id != 1 && !this.pingReceived) {
            //this.connection.sendPacketAndClose(NO_PONG);
            //return;
        }*/
        this.keepAliveReceived = true;
        this.disconnectTask.cancel(false);
        long delay = System.currentTimeMillis() - this.keepAliveSentTime;//the delay for both ways in millis
        //max wait time - network two-way delay + 150 millis for client ticks + 200 millis in back-up
        long maxWaitTime = Math.min(delay + 350, 4000);
        this.scheduleDisconnectTask(maxWaitTime, TimeUnit.MILLISECONDS);
        this.sent = System.currentTimeMillis();
        Log.error("delay: " + delay + " maxWaitTime: " + maxWaitTime);

        /*if (id == 5) {
            Log.error("KEEP ALIVE MOVEMENTS RECEIVED: " + this.movementsReceived);
            this.disconnectTask = null;
            return;
        }

        this.duplexHandler.writeAndFlushPacket(KeepAlives.KEEP_ALIVES[id]);
        this.expectedId = id + 1;
        this.disconnectTask = this.connection.getChannel().eventLoop().schedule(() -> this.connection.sendPacketAndClose(TIMED_OUT), 3, TimeUnit.SECONDS);*/
    }

    public void handle(PacketPlayInTransaction transaction) {

    }

    private long lastKeepAliveSentTime;
    private double deltaY, lastDeltaY;
    private boolean isFalling;

    public void handle(FlyingPacket flying) {
        WrapperPlayClientPlayerFlying wrapper = flying.wrapper();

        if (wrapper.isOnGround()) {
            this.connection.sendPacketAndClose(INVALID_GROUND);
            return;
        }

        Location loc = wrapper.getLocation();

        if (wrapper.hasRotationChanged()) {
            this.yaw = loc.getYaw();
            this.pitch = loc.getPitch();
        }

        if (wrapper.hasPositionChanged()) {

            double lastY = this.y;

            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();

            this.lastDeltaY = deltaY;
            this.deltaY = this.y - lastY;
        }

        //the player does not have the ability to move on X, Z
        //and is spawned on X=0, Z=0
        if (this.x != 0 || this.z != 0) {
            this.connection.sendPacketAndClose(INVALID_XZ);
            return;
        }

        if (!wrapper.hasPositionChanged() && this.isFalling/*|| wrapper.hasPositionChanged() && !this.isFalling*/) {
            this.connection.sendPacketAndClose(INVALID_POS_CHANGE);
            return;
        }

        Log.error("LOC: X: %s Y: %s Z: %s YAW: %s PITCH: %s", x, y, z, yaw, pitch);

        if (this.movementsSent) {

            long now = System.currentTimeMillis();
            long lastKeepAliveSent = now - lastKeepAliveSentTime;

            if (lastKeepAliveSent >= 15000) {
                this.duplexHandler.writeAndFlushPacket(KeepAlives.KEEP_ALIVE_PREVENT_TIMEOUT);
                this.lastKeepAliveSentTime = now;
            }
            return;
        }

        this.movementsReceived++;
        if (this.movementsReceived >= 4) {
            double predictedDeltaY = (this.lastDeltaY - 0.08) * 0.98;
            double diff = this.deltaY - predictedDeltaY;
            //Log.error("DELTA Y: %s PREDICTED: %s DIFF: %s", deltaY, predictedDeltaY, diff);

            //a fair maximum deviation
            if (Math.abs(diff) > 1E-5) {
                this.connection.sendPacketAndClose(INVALID_DELTA_Y);
                return;
            }
        }
        switch (this.movementsReceived) {
            case 1: {
                if (this.tpConfirmsReceived != 1) {
                    this.connection.sendPacketAndClose(INVALID_MOVE);
                    return;
                }
                //Log.error("Y: " + this.y + " TELEPORT_Y: " + TELEPORT_Y + " EQUAL: " + (TELEPORT_Y == this.y));
                if ((int) this.y != TELEPORT_Y) {
                    this.connection.sendPacketAndClose(INVALID_Y);
                    return;
                }
                return;
            }
            case 2: {
                if (this.tpConfirmsReceived != 2) {
                    this.connection.sendPacketAndClose(INVALID_MOVE);
                    return;
                }
                //Log.error("Y: " + this.y + " TELEPORT_Y: " + TELEPORT_Y + " EQUAL: " + (TELEPORT_VALID_Y == this.y));
                if ((int) this.y != TELEPORT_VALID_Y) {
                    this.connection.sendPacketAndClose(INVALID_Y);
                    return;
                }
                if (!this.keepAliveReceived) {
                    this.connection.sendPacketAndClose(NO_KEEP_ALIVE);
                    return;
                }
                //this.duplexHandler.writePacket(PacketSnapshots.PACKET_PLAYER_POS_AND_LOOK_VALID);

                long d = System.currentTimeMillis() - sent;

                Log.error("RECEIVED MOVES IN: " + d + "ms");
                this.disconnectTask.cancel(false);
                return;
            }
            case 3: {
                //same as the second movement, the client will start falling only after sending this packet
                if ((int) this.y != TELEPORT_VALID_Y) {
                    this.connection.sendPacketAndClose(INVALID_Y);
                    return;
                }
                if (!this.pongReceived) {
                    this.connection.sendPacketAndClose(NO_PONG);
                    return;
                }
                this.isFalling = true;
                return;
            }
            case 10: {
                this.movementsSent = true;
                this.isFalling = false;
                this.duplexHandler.writeAndFlushPacket(PLAYER_ABILITIES_FLY);
                //long d = System.currentTimeMillis() - sent;

                //Log.error("ALL 10 MOVES IN: " + d + "ms");
            }
        }
        //this.duplexHandler.writeAndFlushPacket(new PacketPlayOutPing().setId(ThreadLocalRandom.current().nextInt()));
        //this.duplexHandler.writeAndFlushPacket(new PacketPlayOutKeepAlive().setId(ThreadLocalRandom.current().nextInt()));
    }
}