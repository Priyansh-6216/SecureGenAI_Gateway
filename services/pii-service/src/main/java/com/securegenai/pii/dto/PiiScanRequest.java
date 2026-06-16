package com.securegenai.pii.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PiiScanRequest {
    @NotBlank
    private String text;
}
