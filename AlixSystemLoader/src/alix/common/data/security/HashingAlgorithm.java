package alix.common.data.security;

public interface HashingAlgorithm {

    String hash(String s);

    byte hashId();

}