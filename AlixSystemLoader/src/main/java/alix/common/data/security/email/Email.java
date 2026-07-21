package alix.common.data.security.email;

import alix.common.antibot.captcha.secrets.files.UserTokensFileManager;
import alix.common.data.crypto.EncryptedSequence;
import alix.common.utils.other.keys.secret.MapSecretKey;

import java.util.UUID;

public final class Email {

    private final EncryptedSequence email;

    Email(EncryptedSequence email) {
        this.email = email;
    }

    public static Email readFromSaved(String line, MapSecretKey<UUID> key) throws Exception {
        return line.equals("0") ? null : new Email(EncryptedSequence.fromEncrypted(line, UserTokensFileManager.getTokenOrSupply(key)));
    }

    public static Email fromEmail(String email, MapSecretKey<UUID> key) throws Exception {
        return new Email(EncryptedSequence.fromUnencrypted(email, UserTokensFileManager.getTokenOrSupply(key)));
    }

    public String email() {
        return this.email.decrypted();
    }

    public String toSavable() {
        return this.email.encrypted();
    }
}