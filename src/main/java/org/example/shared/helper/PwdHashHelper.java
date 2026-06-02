package org.example.shared.helper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PwdHashHelper {
    public static String hashPwd(String original_pwd) {
        StringBuilder encrypted_pwd;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = md.digest(original_pwd.getBytes(StandardCharsets.UTF_8));
            encrypted_pwd = new StringBuilder(2*encodedHash.length);
            for(byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) encrypted_pwd.append('0');
                encrypted_pwd.append(hex);
            }
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return encrypted_pwd.toString();
    }
}
