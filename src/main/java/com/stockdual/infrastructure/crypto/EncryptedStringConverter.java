package com.stockdual.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String ENV_KEY_NAME = "STOCKDUAL_ENCRYPTION_KEY";

    private final byte[] keyBytes;

    public EncryptedStringConverter() {
        String envKey = System.getenv(ENV_KEY_NAME);
        if (envKey == null || envKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "Variable de entorno " + ENV_KEY_NAME + " no está configurada. " +
                "La clave de encriptación debe proporcionarse para usar AES/GCM."
            );
        }
        // Normalize key to exactly 32 bytes (256 bits AES key)
        byte[] rawBytes = envKey.getBytes(StandardCharsets.UTF_8);
        byte[] cleanBytes = new byte[32];
        System.arraycopy(rawBytes, 0, cleanBytes, 0, Math.min(rawBytes.length, cleanBytes.length));
        this.keyBytes = cleanBytes;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] encryptedData = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, combined, IV_LENGTH, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error encrypting field: {}", e.getMessage(), e);
            throw new RuntimeException("Error encrypting field", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            if (combined.length < IV_LENGTH) {
                return dbData; // Return as-is if it doesn't match GCM format length
            }

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

            int encryptedSize = combined.length - IV_LENGTH;
            byte[] encryptedData = new byte[encryptedSize];
            System.arraycopy(combined, IV_LENGTH, encryptedData, 0, encryptedSize);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Fallback for unencrypted legacy data or key mismatch
            log.debug("Decryption failed, falling back to plain text database value: {}", e.getMessage());
            return dbData;
        }
    }
}
