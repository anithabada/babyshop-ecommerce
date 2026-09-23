package com.babyshop.util;

import com.babyshop.config.AppConfig;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Minimal, dependency-free JSON Web Token (HS256) implementation.
 * A token is issued on login/register and must be sent by the frontend as:
 *   Authorization: Bearer <token>
 *
 * Structure: base64url(header) + "." + base64url(payload) + "." + base64url(HMAC-SHA256 signature)
 */
public final class JwtUtil {

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private JwtUtil() {}

    public static String generateToken(String subjectEmail) {
        long now = System.currentTimeMillis();
        long exp = now + AppConfig.JWT_EXPIRATION_MS;

        JsonObject payload = new JsonObject();
        payload.addProperty("sub", subjectEmail);
        payload.addProperty("iat", now / 1000);
        payload.addProperty("exp", exp / 1000);

        String headerB64 = ENCODER.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = ENCODER.encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;
        String signature = sign(signingInput);

        return signingInput + "." + signature;
    }

    /** Returns the subject (email) if the token is valid and not expired, otherwise null. */
    public static String validateAndGetSubject(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!expectedSignature.equals(parts[2])) return null;

            String payloadJson = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject payload = com.google.gson.JsonParser.parseString(payloadJson).getAsJsonObject();

            long exp = payload.get("exp").getAsLong();
            if (System.currentTimeMillis() / 1000 > exp) {
                return null; // expired
            }
            return payload.get("sub").getAsString();
        } catch (Exception e) {
            return null; // any parsing/signature error -> treat as invalid token
        }
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(AppConfig.JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawSignature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return ENCODER.encodeToString(rawSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }
}
