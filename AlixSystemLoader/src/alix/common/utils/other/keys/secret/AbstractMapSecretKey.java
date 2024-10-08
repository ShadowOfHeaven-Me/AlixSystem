package alix.common.utils.other.keys.secret;

abstract class AbstractMapSecretKey<T> implements MapSecretKey<T> {

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof MapSecretKey)) return false;

        MapSecretKey other = (MapSecretKey) obj;
        return this.key().equals(other.key());
    }

    @Override
    public final int hashCode() {
        return this.key().hashCode();
    }
}