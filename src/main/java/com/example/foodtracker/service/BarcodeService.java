package com.example.foodtracker.service;

import com.example.foodtracker.repository.ProductCatalogRepository;
import com.example.foodtracker.entity.ProductCatalog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class BarcodeService {

    public static class ProductInfo {
        public String barcode;         // normalized GTIN/EAN/UPC
        public String name;
        public String ingredients;
        public LocalDate expiryDate;   // only when GS1 AI (17) present or OFF explicitly has it
        public Integer shelfLifeDays;  // from local catalog (days)
    }

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();
    private final ProductCatalogRepository catalogRepo;

    public BarcodeService(ProductCatalogRepository catalogRepo) {
        this.catalogRepo = catalogRepo;
    }

    public ProductInfo lookup(String payload) {
        if (payload == null || payload.isBlank()) return null;

        // 1) Try GS1 (Digital Link / parentheses / raw GS) first
        Map<String, String> gs1 = parseGs1(payload);
        ProductInfo info = new ProductInfo();
        if (!gs1.isEmpty()) {
            info.barcode = gs1.get("01");
            String ai17 = gs1.get("17");
            if (ai17 != null && ai17.matches("\\d{6}")) {
                int yy = Integer.parseInt(ai17.substring(0, 2));
                int mm = Integer.parseInt(ai17.substring(2, 4));
                int dd = Integer.parseInt(ai17.substring(4, 6));
                info.expiryDate = LocalDate.of(2000 + yy, mm, dd);
            }
            if (info.barcode != null) fillFromLocalCatalog(info, info.barcode);
            if (info.barcode != null && info.barcode.matches("\\d{8,14}")) {
                fillFromOpenFoodFacts(info, info.barcode);
            }
            return info;
        }

        // 2) Treat as plain EAN/UPC digits
        String digits = payload.replaceAll("\\D", "");
        if (!digits.matches("\\d{8,14}")) return null;

        List<String> candidates = new ArrayList<>();
        candidates.add(digits);
        // UPC-A 12 → also try leading-zero EAN-13
        if (digits.length() == 12) candidates.add("0" + digits);

        for (String code : candidates) {
            info = new ProductInfo();
            info.barcode = code;
            fillFromLocalCatalog(info, code);
            boolean ok = fillFromOpenFoodFacts(info, code);
            if (ok && (info.name != null || info.ingredients != null || info.shelfLifeDays != null)) {
                return info;
            }
        }

        // Fallback: at least return local data for the original digits
        info = new ProductInfo();
        info.barcode = digits;
        fillFromLocalCatalog(info, digits);
        fillFromOpenFoodFacts(info, digits);
        return info;
    }

    private void fillFromLocalCatalog(ProductInfo info, String code) {
        catalogRepo.findByBarcode(code).ifPresent(pc -> {
            if (info.name == null) info.name = pc.getName();
            if (info.ingredients == null) info.ingredients = pc.getIngredients();
            if (info.shelfLifeDays == null) info.shelfLifeDays = pc.getShelfLifeDays();
        });
    }

    private boolean fillFromOpenFoodFacts(ProductInfo info, String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "FoodTracker/1.0 (+contact: you@example.com)");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> req = new HttpEntity<>(headers);

            // v2 first
            ResponseEntity<String> v2 = rest.exchange(
                "https://world.openfoodfacts.org/api/v2/product/" + code + ".json",
                HttpMethod.GET, req, String.class
            );
            if (parseOffResponse(info, v2.getBody())) return true;

            // v0 fallback
            ResponseEntity<String> v0 = rest.exchange(
                "https://world.openfoodfacts.org/api/v0/product/" + code + ".json",
                HttpMethod.GET, req, String.class
            );
            return parseOffResponse(info, v0.getBody());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean parseOffResponse(ProductInfo info, String body) {
        try {
            if (body == null) return false;
            JsonNode root = om.readTree(body);
            JsonNode product = root.path("product");
            if (product.isMissingNode() || product.isNull()) return false;

            String name = firstNonBlank(
                asText(product.path("product_name")),
                asText(product.path("product_name_en")),
                asText(product.path("generic_name")),
                asText(product.path("generic_name_en"))
            );

            String ingredients = firstNonBlank(
                asText(product.path("ingredients_text")),
                asText(product.path("ingredients_text_en"))
            );
            if (ingredients == null) {
                // any language key like ingredients_text_bn etc.
                Iterator<String> it = product.fieldNames();
                while (it.hasNext()) {
                    String f = it.next();
                    if (f.startsWith("ingredients_text_")) {
                        String v = asText(product.path(f));
                        if (v != null && !v.isBlank()) { ingredients = v; break; }
                    }
                }
            }
            if (ingredients == null) {
                // structured array fallback
                JsonNode arr = product.path("ingredients");
                if (arr.isArray()) {
                    List<String> parts = new ArrayList<>();
                    for (JsonNode n : arr) {
                        String t = asText(n.path("text"));
                        if (t != null && !t.isBlank()) parts.add(t);
                    }
                    if (!parts.isEmpty()) ingredients = String.join(", ", parts);
                }
            }

            String expIso = asText(product.path("expiration_date")); // rarely present
            LocalDate exp = null;
            if (expIso != null && expIso.matches("\\d{4}-\\d{2}-\\d{2}")) {
                try { exp = LocalDate.parse(expIso); } catch (Exception ignored) {}
            }

            if (name != null) info.name = name;
            if (ingredients != null) info.ingredients = ingredients;
            if (exp != null) info.expiryDate = exp;

            String code = asText(product.path("code"));
            if (code != null && code.matches("\\d{8,14}")) info.barcode = code;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String asText(JsonNode n) {
        return (n != null && !n.isMissingNode() && !n.isNull()) ? n.asText() : null;
    }

    private String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    /** Parse GS1: Digital Link (/01/..../17/..), parentheses (01)...(17)..., raw GS separators (ASCII 29). */
    public Map<String, String> parseGs1(String s) {
        Map<String, String> map = new HashMap<>();
        if (s == null) return map;

        String data = s.trim();

        // Digital Link style: .../01/<gtin>/17/<yymmdd>/10/<batch>
        if (data.contains("/01/")) {
            String[] parts = data.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if (parts[i].equals("01")) map.put("01", parts[i + 1].replaceAll("[^0-9]", ""));
                if (parts[i].equals("17")) map.put("17", parts[i + 1].replaceAll("[^0-9]", ""));
                if (parts[i].equals("10")) map.put("10", parts[i + 1]);
                if (parts[i].equals("21")) map.put("21", parts[i + 1]);
            }
            if (!map.isEmpty()) return map;
        }

        // Parentheses AI format
        if (data.contains("(") && data.contains(")")) {
            int i = 0;
            while (i < data.length()) {
                int open = data.indexOf('(', i);
                if (open < 0) break;
                int close = data.indexOf(')', open + 1);
                if (close < 0) break;
                String ai = data.substring(open + 1, close).trim();
                int nextOpen = data.indexOf('(', close + 1);
                String value = (nextOpen < 0) ? data.substring(close + 1) : data.substring(close + 1, nextOpen);
                value = value.trim().replaceAll("[\\u001D\\u001E\\u001F]", "");
                map.put(ai, value);
                i = (nextOpen < 0) ? data.length() : nextOpen;
            }
            if (!map.isEmpty()) return map;
        }

        // Raw AI sequence with GS separators
        char GS = 29;
        String raw = new String(s.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
        int idx = 0;
        while (idx + 2 <= raw.length()) {
            String ai = raw.substring(idx, Math.min(idx + 2, raw.length()));
            if (ai.equals("01") && idx + 16 <= raw.length()) {
                String gtin = raw.substring(idx + 2, idx + 16).replaceAll("[^0-9]", "");
                map.put("01", gtin);
                idx += 16;
            } else if (ai.equals("17") && idx + 8 <= raw.length()) {
                String yymmdd = raw.substring(idx + 2, idx + 8).replaceAll("[^0-9]", "");
                map.put("17", yymmdd);
                idx += 8;
            } else if (ai.equals("10")) {
                int start = idx + 2;
                int end = raw.indexOf(GS, start);
                if (end < 0) end = raw.length();
                map.put("10", raw.substring(start, end));
                idx = end + 1;
            } else if (ai.equals("21")) {
                int start = idx + 2;
                int end = raw.indexOf(GS, start);
                if (end < 0) end = raw.length();
                map.put("21", raw.substring(start, end));
                idx = end + 1;
            } else {
                idx++;
            }
        }
        return map;
    }
}
