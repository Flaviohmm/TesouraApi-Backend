package com.tesoura.api.financial;

import com.tesoura.api.appointment.Appointment;
import com.tesoura.api.financial.dto.TransactionResponseDTO;
import com.tesoura.api.shared.config.PlatformCommissionProperties;
import com.tesoura.api.shared.enums.TransactionType;
import com.tesoura.api.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final PlatformCommissionProperties commissionProperties;

    public TransactionService(TransactionRepository transactionRepository, PlatformCommissionProperties commissionProperties) {
        this.transactionRepository = transactionRepository;
        this.commissionProperties = commissionProperties;
    }

    @Transactional
    public void recordAppointmentCompletion(Appointment appointment) {
        log.info("Recording transaction for completed appointment {}", appointment.getId());

        Transaction income = new Transaction();
        income.setSalonId(TenantContext.get());
        income.setAppointmentId(appointment.getId());
        income.setType(TransactionType.INCOME);
        income.setAmount(appointment.getTotalPrice());
        income.setDescription("Receita do agendamento " + appointment.getId());
        transactionRepository.save(income);

        BigDecimal commissionAmount = appointment.getTotalPrice()
                .multiply(commissionProperties.rate())
                .setScale(2, RoundingMode.HALF_UP);

        Transaction commission = new Transaction();
        commission.setSalonId(TenantContext.get());
        commission.setAppointmentId(appointment.getId());
        commission.setType(TransactionType.COMMISSION);
        commission.setAmount(commissionAmount);
        commission.setDescription("Comissão da plataforma Tesoura ("
                + commissionProperties.rate().multiply(BigDecimal.valueOf(100)) + "%) sobre o agendamento "
                + appointment.getId());
        transactionRepository.save(commission);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> listRecent() {
        return transactionRepository.findTop50BySalonIdOrderByCreatedAtDesc(TenantContext.get()).stream()
                .map(TransactionResponseDTO::from)
                .toList();
    }
}
