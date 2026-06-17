package com.securegenai.masking.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PiiEntity {
    private String type;
    private String value;
    private int startIndex;
    private int endIndex;
    private double confidenceScore;
}
