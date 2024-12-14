package alix.common.data.security.password.hashing;

public interface HashingAlgorithm {

    String hash(String s);

    byte hashId();

}