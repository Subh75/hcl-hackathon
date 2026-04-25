package com.favouritepayee.service;

import com.favouritepayee.entity.PayeeInteraction;
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

    public double score(List<PayeeInteraction> interactions, LocalDateTime now) {
        List<PayeeInteraction> safeInteractions = Optional.ofNullable(interactions).orElseGet(List::of);

        double frequencyScore = safeInteractions.size() * scoringProperties.frequencyWeight();
        double recencyScore = safeInteractions.stream()
                .mapToDouble(interaction -> {
                    long daysSince = Math.max(
                            0,
                            ChronoUnit.DAYS.between(interaction.getInteractedAt().toLocalDate(), now.toLocalDate())
                    );
                    return 1.0 / (daysSince + 1);
                })
                .sum() * scoringProperties.recencyWeight();
        double timeScore = safeInteractions.stream()
                .filter(interaction -> interaction.getInteractedAt().getHour() / 4 == now.getHour() / 4)
                .count() * scoringProperties.timeWeight();

        return frequencyScore + recencyScore + timeScore;
    }
}
