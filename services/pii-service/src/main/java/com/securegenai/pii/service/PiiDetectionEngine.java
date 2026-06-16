package com.securegenai.pii.service;
import com.securegenai.pii.dto.PiiEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PiiDetectionEngine {

    // Common regex patterns for PII
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}");
    private static final Pattern SSN_PATTERN = Pattern.compile("^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    public List<PiiEntity> scanText(String text) {
        List<PiiEntity> results = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return results;
        }

        scanWithRegex(text, EMAIL_PATTERN, "EMAIL", 0.95, results);
        scanWithRegex(text, PHONE_PATTERN, "PHONE", 0.85, results);
        scanWithRegex(text, SSN_PATTERN, "SSN", 0.99, results);
        scanWithRegex(text, CREDIT_CARD_PATTERN, "CREDIT_CARD", 0.95, results);
        
        // Basic heuristic for addresses (very simplified for MVP)
        Pattern addressPattern = Pattern.compile("(?i)\\d{1,5}\\s+\\w+\\s+(Street|St|Avenue|Ave|Boulevard|Blvd|Road|Rd)");
        scanWithRegex(text, addressPattern, "ADDRESS", 0.70, results);

        return results;
    }

    private void scanWithRegex(String text, Pattern pattern, String type, double confidence, List<PiiEntity> results) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            results.add(new PiiEntity(
                    type,
                    matcher.group(),
                    matcher.start(),
                    matcher.end(),
                    confidence
            ));
        }
    }
}
