package com.page.example.paging;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Small standalone bcrypt generator using jBCrypt so it can be executed without Spring classes on the classpath.
 * Usage: mvn exec:java -Dexec.mainClass=com.page.example.paging.SimpleBcrypt -Dexec.args="plaintext"
 */
public class SimpleBcrypt {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: SimpleBcrypt <password>");
            System.exit(2);
        }
        String plain = args[0];
        String hash = BCrypt.hashpw(plain, BCrypt.gensalt());
        System.out.println(hash);
    }
}
