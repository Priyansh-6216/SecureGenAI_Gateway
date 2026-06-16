package com.securegenai.gateway.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "risk_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;
    @Column(nullable = false)
    private String severity;
    private LocalDateTime createdAt = LocalDateTime.now();
}
