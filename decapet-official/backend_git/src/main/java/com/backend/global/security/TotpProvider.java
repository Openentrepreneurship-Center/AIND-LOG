package com.backend.global.security;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import com.backend.global.error.exception.InternalServerErrorException;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Component
public class TotpProvider {

    private static final int SECRET_SIZE = 20;
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int ALLOWED_TIME_DRIFT = 1;

    private final SecureRandom random = new SecureRandom();
    private final Base32 base32 = new Base32();

    public String generateSecret() {
        byte[] buffer = new byte[SECRET_SIZE];
        random.nextBytes(buffer);
        return base32.encodeToString(buffer);
    }

    public String generateQrCodeUri(String secret, String email, String issuer) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, email, secret, issuer, CODE_DIGITS, TIME_STEP_SECONDS
        );
    }

    public boolean verifyCode(String secret, String code) {
        return validateCode(secret, code);
    }

    public boolean validateCode(String secret, String code) {
        if (secret == null || code == null || code.length() != CODE_DIGITS) {
            return false;
        }

        try {
            int providedCode = Integer.parseInt(code);
            long currentTime = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;

            for (int i = -ALLOWED_TIME_DRIFT; i <= ALLOWED_TIME_DRIFT; i++) {
                int expectedCode = generateCode(secret, currentTime + i);
                if (providedCode == expectedCode) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    private int generateCode(String secret, long timeStep) {
        try {
            byte[] key = base32.decode(secret);
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (timeStep & 0xff);
                timeStep >>= 8;
            }

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0f;
            int truncatedHash = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);

            return truncatedHash % (int) Math.pow(10, CODE_DIGITS);
        } catch (Exception e) {
            log.error("TOTP code generation failed", e);
            throw new InternalServerErrorException();
        }
    }
}
