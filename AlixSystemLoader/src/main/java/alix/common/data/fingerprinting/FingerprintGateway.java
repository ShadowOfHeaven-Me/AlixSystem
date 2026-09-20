package alix.common.data.fingerprinting;

import io.netty.channel.Channel;

import java.util.function.Consumer;

/**
 * The user-facing entry point into device-fingerprinting ("Device PassKey"): both /account's "Create a
 * Device PassKey" login-setting item and the passkey option of the "Recover account?" login-time gui call
 * through here. Not yet implemented - only the call sites and the callback shape exist so far, gated
 * behind {@link alix.common.utils.config.ConfigParams#fingerprintingEnabled} the same way every other piece
 * of this abstraction is. See FingerprintManager/FingerprintBuilder for the actual (in-progress) detection
 * mechanism this is expected to eventually drive.
 */
public final class FingerprintGateway {

    /**
     * @param channel  the connection to fingerprint - a real backend player's channel when creating a new
     *                 passkey from /account, or a pre-login ClientConnection's channel when verifying one
     *                 during "Recover account?"
     * @param callback completed with true once the device's fingerprint was successfully obtained AND (for
     *                 a verification call) matches what's already on file, false otherwise - the caller
     *                 decides what "matches" means for its own use case (create vs. verify)
     */
    public static void sendFingerprintingPacks(Channel channel, Consumer<Boolean> callback) {
        //TODO(Shadow): not yet implemented - wire in the real fingerprinting protocol here.
    }

    private FingerprintGateway() {
    }
}
