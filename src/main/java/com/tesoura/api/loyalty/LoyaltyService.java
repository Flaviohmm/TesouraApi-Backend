package com.tesoura.api.loyalty;

import com.tesoura.api.client.Client;
import com.tesoura.api.shared.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoyaltyService {

    private static final BigDecimal POINTS_PER_CURRENCY_UNIT = BigDecimal.TEN;
    private static final BigDecimal POINT_VALUE = new BigDecimal("0.10");

    private final LoyaltyTransactionRepository transactionRepository;

    public LoyaltyService(LoyaltyTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public int pointsEarnedFor(BigDecimal amountSpent) {
        if (amountSpent == null || amountSpent.signum() <= 0) {
            return 0;
        }
        return amountSpent.divide(POINTS_PER_CURRENCY_UNIT, 0, RoundingMode.DOWN).intValue();
    }

    public BigDecimal discountForPoints(int points) {
        if (points <= 0) {
            return BigDecimal.ZERO;
        }
        return POINT_VALUE.multiply(BigDecimal.valueOf(points));
    }

    @Transactional
    public void credit(Client client, int points, String reason) {
        if (points <= 0) {
            return;
        }
        client.setLoyaltyPoints(client.getLoyaltyPoints() + points);
        record(client, points, reason);
    }

    @Transactional
    public void debit(Client client, int points, String reason) {
        if (points <= 0) {
            return;
        }
        if (points > client.getLoyaltyPoints()) {
            throw ApiException.badRequest("Cliente não possui pontos de fidelidade suficientes");
        }
        client.setLoyaltyPoints(client.getLoyaltyPoints() - points);
        record(client, -points, reason);
    }

    private void record(Client client, int signedPoints, String reason) {
        LoyaltyTransaction entry = new LoyaltyTransaction();
        entry.setClient(client);
        entry.setPoints(signedPoints);
        entry.setReason(reason);
        transactionRepository.save(entry);
    }
}
