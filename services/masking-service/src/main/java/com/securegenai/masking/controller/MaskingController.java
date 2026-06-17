package com.securegenai.masking.controller;
import com.securegenai.masking.dto.MaskingRequest;
import com.securegenai.masking.dto.MaskingResponse;
import com.securegenai.masking.service.MaskingEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mask")
@RequiredArgsConstructor
public class MaskingController {

    private final MaskingEngine maskingEngine;

    @PostMapping
    public ResponseEntity<MaskingResponse> maskData(@Valid @RequestBody MaskingRequest request) {
        String masked = maskingEngine.maskText(request.getOriginalText(), request.getEntitiesToMask());
        return ResponseEntity.ok(new MaskingResponse(masked));
    }
}
