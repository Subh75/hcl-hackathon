package com.favouritepayee.controller;

import com.favouritepayee.dto.ScoringItemDto;
import com.favouritepayee.service.ScoringService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScoringController {

    private final ScoringService scoringService;

    public ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @GetMapping("/scoring/{customerId}")
    public ResponseEntity<List<ScoringItemDto>> score(@PathVariable Long customerId) {
        return ResponseEntity.ok(scoringService.scoreForCustomer(customerId));
    }
}
