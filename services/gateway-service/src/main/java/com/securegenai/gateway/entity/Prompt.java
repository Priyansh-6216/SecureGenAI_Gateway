package com.securegenai.gateway.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "prompts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "original_text", nullable = false)
    private String originalText;
    @Column(name = "masked_text")
    private String maskedText;
    @ManyToOne
    @JoinColumn(name = "risk_assessment_id")
    private RiskAssessment riskAssessment;
    @Column(nullable = false)
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();
}
