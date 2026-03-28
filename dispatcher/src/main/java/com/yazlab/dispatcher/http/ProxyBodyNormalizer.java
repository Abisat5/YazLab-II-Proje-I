package com.yazlab.dispatcher.http;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gecerli JSON: {"username":"a","password":"b"}
 * Bazi istemciler: {username:"a"} veya JavaScript literal (tirnaksiz anahtar) gonderir;
 * standart Jackson 'u' harfinde patlar. Gevsetilmis okuyucu + tekrar yaz: her zaman gecerli JSON bayt.
 */
public final class ProxyBodyNormalizer {

    /** Tirnaksiz alan adi ve tek tirnakli string (Postman / JS) */
    private static final ObjectMapper RELAXED = new ObjectMapper(
            JsonFactory.builder()
                    .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
                    .build());

    private ProxyBodyNormalizer() {
    }

    public static byte[] toJson(HttpServletRequest request, ObjectMapper mapper) throws IOException {
        byte[] raw = request.getInputStream().readAllBytes();
        return rawBytesToJsonObject(raw, mapper);
    }

    private static byte[] rawBytesToJsonObject(byte[] raw, ObjectMapper mapper) throws IOException {
        if (raw == null || raw.length == 0) {
            throw new IllegalArgumentException("Bos govde");
        }
        int start = 0;
        if (raw.length >= 3 && raw[0] == (byte) 0xEF && raw[1] == (byte) 0xBB && raw[2] == (byte) 0xBF) {
            start = 3;
        }
        String text = new String(raw, start, raw.length - start, StandardCharsets.UTF_8).trim();
        if (text.startsWith("{")) {
            try {
                JsonNode tree = RELAXED.readTree(text);
                return RELAXED.writeValueAsBytes(tree);
            } catch (JsonProcessingException e) {
                Map<String, String> fromForm = parseFormUrlEncoded(text);
                if (!fromForm.isEmpty()) {
                    return mapper.writeValueAsBytes(fromForm);
                }
                throw new IllegalArgumentException("Gecersiz govde: " + e.getOriginalMessage());
            }
        }
        Map<String, String> fromForm = parseFormUrlEncoded(text);
        if (fromForm.isEmpty()) {
            throw new IllegalArgumentException("Gecersiz govde: JSON veya key=value form bekleniyor");
        }
        return mapper.writeValueAsBytes(fromForm);
    }

    private static Map<String, String> parseFormUrlEncoded(String text) {
        Map<String, String> m = new LinkedHashMap<>();
        for (String part : text.split("&")) {
            if (part.isEmpty()) {
                continue;
            }
            int eq = part.indexOf('=');
            if (eq > 0) {
                String k = part.substring(0, eq).trim();
                String v = URLDecoder.decode(part.substring(eq + 1).trim(), StandardCharsets.UTF_8);
                m.put(k, v);
            }
        }
        return m;
    }
}
