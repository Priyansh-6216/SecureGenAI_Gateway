package com.securegenai.pii.controller;
import com.securegenai.pii.dto.PiiScanRequest;
import com.securegenai.pii.dto.PiiScanResponse;
import com.securegenai.pii.service.PiiDetectionEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pii")
@RequiredArgsConstructor
public class PiiController {

    private final PiiDetectionEngine piiDetectionEngine;

    @PostMapping("/scan")
    public ResponseEntity<PiiScanResponse> scanForPii(@Valid @RequestBody PiiScanRequest request) {
        var entities = piiDetectionEngine.scanText(request.getText());
        return ResponseEntity.ok(new PiiScanResponse(entities));
    }
}
