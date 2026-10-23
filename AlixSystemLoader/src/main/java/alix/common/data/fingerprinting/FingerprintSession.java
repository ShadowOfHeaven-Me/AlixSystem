package alix.common.data.fingerprinting;

import alix.common.data.PersistentUserData;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientResourcePackStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResourcePackSend;
import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;
import ua.nanit.limbo.connection.pipeline.encryption.CipherHandler;
import ua.nanit.limbo.protocol.packets.PacketUtils;
import ua.nanit.limbo.protocol.packets.play.resourcepack.PacketPlayOutResourcePack;
import ua.nanit.limbo.protocol.registry.Version;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class FingerprintSession {

    private enum Phase {
        TEST,
        DELIVERY,
        FINISHED
    }

    private final Channel channel;
    private final String sessionName;
    private final String host;
    private final int port;
    private final Version version;
    private final PersistentUserData data;
    private final Consumer<Boolean> callback;
    private final CipherHandler cipher;
    private final boolean isVerification;

    private volatile boolean testMode = true;
    private final AtomicInteger downloadRequestedMask = new AtomicInteger(0);
    private int deliveryFingerprint;

    private final AtomicBoolean completed = new AtomicBoolean(false);
    private ScheduledFuture<?> timeoutFuture;
    private Phase currentPhase = Phase.TEST;

    public FingerprintSession(Channel channel, String sessionName, String host, int port,
                              Version version, PersistentUserData data, Consumer<Boolean> callback) {
        this.channel = channel;
        this.sessionName = sessionName;
        this.host = host;
        this.port = port;
        this.version = version;
        this.data = data;
        this.callback = callback;
        this.isVerification = data != null && data.hasFingerprint();

        this.cipher = CipherHandler.encryptionFor(channel);
    }

    public boolean isTestMode() {
        return this.testMode;
    }

    public void markDownloadRequested(int bit) {
        if (bit >= 0 && bit < FingerprintManager.LEN) {
            this.downloadRequestedMask.updateAndGet(mask -> mask | (1 << bit));
        }
    }

    private final AtomicInteger cnt = new AtomicInteger();

    public void onResourcePackStatus(UUID packId, WrapperPlayClientResourcePackStatus.Result result) {
        if (result == null || result == WrapperPlayClientResourcePackStatus.Result.ACCEPTED || result == WrapperPlayClientResourcePackStatus.Result.DOWNLOADED) {
            return;
        }

        Integer bit = FingerprintManager.getIndex(packId);
        if (bit == null || bit < 0 /*|| bit >= this.terminalReceived.length*/) {
            return;
        }

        if (this.cnt.incrementAndGet() != 32) return;

        this.channel.eventLoop().execute(this::onPhaseCompleted);
    }

    public void start() {
        this.channel.eventLoop().execute(() -> {
            this.sendTestPacks();
            this.scheduleTimeout(5, TimeUnit.SECONDS);
        });
    }

    private void scheduleTimeout(long delay, TimeUnit unit) {
        if (this.timeoutFuture != null) {
            this.timeoutFuture.cancel(false);
        }
        this.timeoutFuture = this.channel.eventLoop().schedule(this::onPhaseCompleted, delay, unit);
    }

    private void writeResourcePack(UUID uuid, String url, String hash) {
        var wrapper = new WrapperPlayServerResourcePackSend(uuid, url, hash, false, null);
        var packet = new PacketPlayOutResourcePack(wrapper);
        PacketUtils.write(this.channel, this.version, packet, this.cipher);
        /*var duplex = this.channel.pipeline().get(PacketDuplexHandler.class);
        if (duplex != null) {
        } else {
            PacketEvents.getAPI().getProtocolManager().sendPacket(this.channel, wrapper);
        }*/
    }

    private void sendTestPacks() {
        for (int i = 0; i < FingerprintManager.LEN; i++) {
            String url = "http://" + this.host + ":" + this.port + "/fp/" + this.sessionName + "/" + i;
            String hash = FingerprintManager.SHA1[i];
            UUID uuid = FingerprintManager.BIT_UUIDS[i];
            this.writeResourcePack(uuid, url, hash);
        }
        this.channel.flush();
    }

    private void onPhaseCompleted() {
        if (this.timeoutFuture != null) {
            this.timeoutFuture.cancel(false);
            this.timeoutFuture = null;
        }

        if (this.currentPhase == Phase.TEST) {
            this.currentPhase = Phase.DELIVERY;
            this.onTestPhaseCompleted();
        } else if (this.currentPhase == Phase.DELIVERY) {
            this.currentPhase = Phase.FINISHED;
            this.onDeliveryPhaseCompleted();
        }
    }

    private void onTestPhaseCompleted() {
        // Bit 1 signifies that the pack is cached locally (no web download request sent)
        int detectedFp = ~this.downloadRequestedMask.get();

        // If client had all 32 resource packs cached:
        if (detectedFp == -1) {
            this.sendCannotFingerprintMessage();
            this.finish(false);
            return;
        }

        if (this.isVerification) {
            boolean matches = (detectedFp == this.data.getFingerprint());
            this.finish(matches);
            return;
        }

        // Creation mode:
        if (detectedFp != 0) {
            // Already has a non-zero local fingerprint: adopt it as his fingerprint value
            this.data.setFingerprint(detectedFp);
            this.finish(true);
            return;
        }

        // No fingerprint cached locally: generate new candidate
        int newFp = FingerprintManager.generateNewFingerprint();
        this.testMode = false;
        this.deliveryFingerprint = newFp;

        int count = Integer.bitCount(newFp);
        this.cnt.set(0);

        // Send delivery packs for bits that are 1
        for (int i = 0; i < FingerprintManager.LEN; i++) {
            if ((newFp & (1 << i)) != 0) {
                String url = "http://" + this.host + ":" + this.port + "/fp/" + this.sessionName + "/" + i;
                String hash = FingerprintManager.SHA1[i];
                UUID uuid = FingerprintManager.BIT_UUIDS[i];
                this.writeResourcePack(uuid, url, hash);
            }
        }
        this.channel.flush();

        this.scheduleTimeout(7, TimeUnit.SECONDS);
    }

    private void onDeliveryPhaseCompleted() {
        if (this.data != null) {
            this.data.setFingerprint(this.deliveryFingerprint);
        }
        this.finish(true);
    }

    private void sendCannotFingerprintMessage() {
        FingerprintGateway.sendCannotFingerprintMessage(this.channel, this.cipher, this.version);
    }

    void finish(boolean success) {
        if (this.completed.compareAndSet(false, true)) {
            if (this.timeoutFuture != null) {
                this.timeoutFuture.cancel(false);
                this.timeoutFuture = null;
            }
            this.currentPhase = Phase.FINISHED;
            FingerprintGateway.cleanupSession(this.channel, this.sessionName);
            if (this.callback != null) {
                this.callback.accept(success);
            }
        }
    }

    public void abort() {
        this.finish(false);
    }
}