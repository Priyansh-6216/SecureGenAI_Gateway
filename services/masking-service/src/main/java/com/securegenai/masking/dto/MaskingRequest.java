package com.securegenai.masking.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class MaskingRequest {
    @NotBlank
    private String originalText;
    private List<PiiEntity> entitiesToMask;
}
