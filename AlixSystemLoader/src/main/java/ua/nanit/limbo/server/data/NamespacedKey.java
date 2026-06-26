package ua.nanit.limbo.server.data;

import lombok.Data;

import java.util.Locale;

@Data
public class NamespacedKey {

    private static final String MINECRAFT_NAMESPACE = "minecraft";

    private String namespace;
    private String key;

    public static NamespacedKey minecraft(String key) {
        NamespacedKey namespacedKey = new NamespacedKey();
        namespacedKey.setNamespace(MINECRAFT_NAMESPACE);
        namespacedKey.setKey(key.toLowerCase(Locale.ROOT));
        return namespacedKey;
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.key;
    }
}