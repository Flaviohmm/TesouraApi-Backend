package com.tesoura.api.financial;

import com.tesoura.api.appointment.Appointment;
import com.tesoura.api.shared.enums.TransactionType;
import com.tesoura.api.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
        income.setReferenceId(appointment.getId());
        transactionRepository.save(income);
    }
}
