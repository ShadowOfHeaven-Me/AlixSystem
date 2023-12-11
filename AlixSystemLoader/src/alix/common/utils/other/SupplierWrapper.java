package alix.common.utils.other;

@FunctionalInterface
public interface SupplierWrapper<T, Q> {

    T get(Q q);

}