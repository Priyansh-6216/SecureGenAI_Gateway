package com.securegenai.pii.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PiiScanResponse {
    private List<PiiEntity> entities;
}
