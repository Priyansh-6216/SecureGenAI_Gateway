package com.securegenai.masking.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MaskingResponse {
    private String maskedText;
}
