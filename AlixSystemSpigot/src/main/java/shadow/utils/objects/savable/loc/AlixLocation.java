package shadow.utils.objects.savable.loc;

public interface AlixLocation<T, K> {

    void teleport(T player);

    K getId();

    String toSavable();

}