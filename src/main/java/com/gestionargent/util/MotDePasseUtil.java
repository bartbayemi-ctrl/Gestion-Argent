package com.gestionargent.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hachage du mot de passe/PIN avec sel aléatoire (SHA-256).
 */
public final class MotDePasseUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private MotDePasseUtil() {
    }

    public static String genererSel() {
        byte[] sel = new byte[16];
        RANDOM.nextBytes(sel);
        return Base64.getEncoder().encodeToString(sel);
    }

    public static String hacher(String motDePasse, String sel) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(sel));
            byte[] hash = digest.digest(motDePasse.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithme de hachage indisponible", e);
        }
    }

    public static boolean verifier(String motDePasseSaisi, String sel, String hashAttendu) {
        String hashSaisi = hacher(motDePasseSaisi, sel);
        return MessageDigest.isEqual(hashSaisi.getBytes(), hashAttendu.getBytes());
    }
}
