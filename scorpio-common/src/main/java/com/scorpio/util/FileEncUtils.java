package com.scorpio.util;

import com.scorpio.Constants;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class FileEncUtils {

    private FileEncUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void encrypt(String key, InputStream is, OutputStream os) {
        encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, is, os);
    }

    public static void decrypt(String key, InputStream is, OutputStream os) {
        encryptOrDecrypt(key, Cipher.DECRYPT_MODE, is, os);
    }

    public static void encryptOrDecrypt(String key, int mode, InputStream is, OutputStream os) {
        try {
            SecretKeySpec sks = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(Cipher.ENCRYPT_MODE, sks);
                CipherInputStream cis = new CipherInputStream(is, cipher);
                doCopy(cis, os);
            } else if (mode == Cipher.DECRYPT_MODE) {
                cipher.init(Cipher.DECRYPT_MODE, sks);
                CipherOutputStream cos = new CipherOutputStream(os, cipher);
                doCopy(is, cos);
            } else {
                throw new UnsupportedOperationException("parameter mode is invalid.");
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException
                 | NoSuchPaddingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static void doCopy(InputStream is, OutputStream os) throws IOException {
        byte[] bytes = new byte[Constants.BUFFER_DEFAULT_SIZE];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }
}
