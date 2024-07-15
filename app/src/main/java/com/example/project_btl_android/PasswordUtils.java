package com.example.project_btl_android;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    public static String encryptPassword(String password) {
        try {
            // Tạo MessageDigest instance cho SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Thêm password bytes để băm
            byte[] encodedhash = digest.digest(password.getBytes());

            // Chuyển byte array sang dạng Hex String
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

