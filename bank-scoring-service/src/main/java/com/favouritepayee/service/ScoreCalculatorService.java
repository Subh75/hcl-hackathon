package com.favouritepayee.service;

import com.favouritepayee.dto.RawInteractionDto;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculatorService {

    private final ScoringProperties scoringProperties;

    public ScoreCalculatorService(ScoringProperties scoringProperties) {
        this.scoringProperties = scoringProperties;
    }

    public double score(List<RawInteractionDto> interactions, LocalDateTime now) {
        List<RawInteractionDto> safeInteractions = Optional.ofNullable(interactions).orElseGet(List::of);

        double frequencyScore = safeInteractions.size() * scoringProperties.frequencyWeight();
        double recencyScore = safeInteractions.stream()
                .mapToDouble(interaction -> {
                    long daysSince = Math.max(
                            0,
                            ChronoUnit.DAYS.between(interaction.interactedAt().toLocalDate(), now.toLocalDate())
                    );
                    return 1.0 / (daysSince + 1);
                })
                .sum() * scoringProperties.recencyWeight();
        double timeScore = safeInteractions.stream()
                .filter(interaction -> interaction.interactedAt().getHour() / 4 == now.getHour() / 4)
                .count() * scoringProperties.timeWeight();

        return frequencyScore + recencyScore + timeScore;
    }
}
