package com.securegenai.policy.dto;
import lombok.Data;

@Data
public class PiiEntity {
    private String type;
    private String value;
    private int startIndex;
    private int endIndex;
    private double confidenceScore;
}
