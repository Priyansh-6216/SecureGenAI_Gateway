package com.securegenai.masking.service;
import com.securegenai.masking.dto.PiiEntity;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class MaskingEngine {

    public String maskText(String originalText, List<PiiEntity> entities) {
        if (originalText == null || originalText.isBlank() || entities == null || entities.isEmpty()) {
            return originalText;
        }

        // Sort entities by startIndex in reverse order so replacing doesn't shift remaining indexes
        List<PiiEntity> sortedEntities = entities.stream()
                .sorted(Comparator.comparingInt(PiiEntity::getStartIndex).reversed())
                .toList();

        StringBuilder sb = new StringBuilder(originalText);

        for (PiiEntity entity : sortedEntities) {
            if (entity.getStartIndex() < 0 || entity.getEndIndex() > sb.length()) {
                continue; // invalid bounds
            }
            String maskedValue = applyMaskingRule(entity.getType(), entity.getValue());
            sb.replace(entity.getStartIndex(), entity.getEndIndex(), maskedValue);
        }

        return sb.toString();
    }

    private String applyMaskingRule(String type, String value) {
        if (value == null || value.isBlank()) return value;

        return switch (type.toUpperCase()) {
            case "EMAIL" -> {
                int atIndex = value.indexOf('@');
                if (atIndex > 1) {
                    yield value.charAt(0) + "***" + value.substring(atIndex);
                }
                yield "***@***.***";
            }
            case "SSN" -> "***-**-" + (value.length() > 4 ? value.substring(value.length() - 4) : "****");
            case "CREDIT_CARD" -> "****-****-****-" + (value.length() > 4 ? value.substring(value.length() - 4) : "****");
            case "PHONE" -> {
                if (value.length() > 4) {
                    yield "***-***-" + value.substring(value.length() - 4);
                }
                yield "[REDACTED_PHONE]";
            }
            case "ADDRESS" -> "[REDACTED_ADDRESS]";
            default -> "[REDACTED]";
        };
    }
}
