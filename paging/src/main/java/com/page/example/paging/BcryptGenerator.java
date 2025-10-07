package com.page.example.paging;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Tiny CLI helper to produce BCrypt password hashes for seeding users.
 * Usage: java -cp <app-jar-or-classpath> com.page.example.paging.BcryptGenerator myPlainPassword
 */
public class BcryptGenerator {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: BcryptGenerator <password>");
            System.exit(2);
        }

        String plain = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode(plain);
        System.out.println(hashed);
    }
}
