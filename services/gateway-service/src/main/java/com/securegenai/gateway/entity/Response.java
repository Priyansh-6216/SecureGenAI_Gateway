package com.securegenai.gateway.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "prompt_id")
    private Prompt prompt;
    @Column(nullable = false)
    private String provider;
    @Column(name = "response_text")
    private String responseText;
    private LocalDateTime createdAt = LocalDateTime.now();
}
