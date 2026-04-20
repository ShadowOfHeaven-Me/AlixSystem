import at.favre.lib.crypto.bcrypt.BCrypt;

public class Tester {

    public static final BCrypt.Hasher HASHER = BCrypt
            .with(BCrypt.Version.VERSION_2A);
    public static final BCrypt.Verifyer VERIFIER = BCrypt
            .verifyer(BCrypt.Version.VERSION_2A);

    public static void main(String[] args) {
        //var password = Hashing.BCRYPT.hash("sex");
        var word = "sex";
        var password = HASHER.hashToString(10, word.toCharArray());
        boolean match = VERIFIER.verify(word.toCharArray(), password).verified;
        System.out.println(password);
        System.out.println(match);
    }
}