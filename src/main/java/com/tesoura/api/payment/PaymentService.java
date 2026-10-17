package com.tesoura.api.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.tesoura.api.appointment.Appointment;
import com.tesoura.api.appointment.AppointmentService;
import com.tesoura.api.payment.dto.PaymentCheckoutDTO;
import com.tesoura.api.payment.dto.PaymentStatusDTO;
import com.tesoura.api.shared.config.PlatformCommissionProperties;
import com.tesoura.api.shared.enums.AppointmentStatus;
import com.tesoura.api.shared.enums.PaymentStatus;
import com.tesoura.api.shared.exception.ApiException;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import com.tesoura.api.shared.tenant.TenantContext;
import com.tesoura.api.stripe.StripeAccount;
import com.tesoura.api.stripe.StripeAccountRepository;
import com.tesoura.api.stripe.StripeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final StripeAccountRepository stripeAccountRepository;
    private final StripeProperties stripeProperties;
    private final PlatformCommissionProperties commissionProperties;
    private final AppointmentService appointmentService;

    public PaymentService(
            PaymentRepository paymentRepository,
            StripeAccountRepository stripeAccountRepository,
            StripeProperties stripeProperties,
            PlatformCommissionProperties commissionProperties,
            AppointmentService appointmentService
    ) {
        this.paymentRepository = paymentRepository;
        this.stripeAccountRepository = stripeAccountRepository;
        this.stripeProperties = stripeProperties;
        this.commissionProperties = commissionProperties;
        this.appointmentService = appointmentService;
    }

    @Transactional
    public PaymentCheckoutDTO createCheckout(String slug, UUID appointmentId) {
        Appointment appointment = appointmentService.require(appointmentId);

        StripeAccount account = stripeAccountRepository.findBySalonId(TenantContext.get())
                .orElseThrow(() -> ApiException.badRequest("Salão ainda não conectou o Stripe"));
        if (!account.isChargesEnabled()) {
            throw ApiException.badRequest("Salão ainda não concluiu a configuração da conta Stripe");
        }

        BigDecimal totalPrice = appointment.getTotalPrice();
        BigDecimal platformFee = totalPrice
                .multiply(commissionProperties.rate())
                .setScale(2, RoundingMode.HALF_UP);

        String externalReference = UUID.randomUUID().toString();
        String returnBase = stripeProperties.frontendReturnUrl() + "/descobrir/salao/" + slug;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setClientReferenceId(externalReference)
                    .setSuccessUrl(returnBase + "?appointmentId=" + appointmentId + "&stripeReturn=1")
                    .setCancelUrl(returnBase)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("brl")
                                    .setUnitAmount(toCents(totalPrice))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Agendamento Tesoura")
                                            .build())
                                    .build())
                            .build())
                    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                            .setApplicationFeeAmount(toCents(platformFee))
                            .setTransferData(SessionCreateParams.PaymentIntentData.TransferData.builder()
                                    .setDestination(account.getAccountId())
                                    .build())
                            .build())
                    .build();

            Session session = Session.create(params);

            Payment payment = new Payment();
            payment.setAppointmentId(appointmentId);
            payment.setExternalReference(externalReference);
            payment.setCheckoutSessionId(session.getId());
            payment.setAmount(totalPrice);
            payment.setPlatformFeeAmount(platformFee);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCheckoutUrl(session.getUrl());
            paymentRepository.save(payment);

            return new PaymentCheckoutDTO(payment.getId().toString(), session.getUrl());
        } catch (StripeException ex) {
            log.error("Failed to create Stripe checkout session", ex);
            throw ApiException.badRequest("Não foi possível iniciar o pagamento online");
        }
    }

    @Transactional(readOnly = true)
    public PaymentStatusDTO getStatus(UUID appointmentId) {
        Payment payment = paymentRepository
                .findTopByAppointmentIdAndSalonIdOrderByCreatedAtDesc(appointmentId, TenantContext.get())
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum pagamento encontrado para este agendamento"));
        return new PaymentStatusDTO(payment.getStatus());
    }

    @Transactional
    public void applyWebhookUpdate(Session session, String eventType) {
        Payment payment = paymentRepository.findByExternalReference(session.getClientReferenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para a referência informada"));

        PaymentStatus previous = payment.getStatus();
        PaymentStatus mapped = mapStatus(session, eventType);

        payment.setPaymentIntentId(session.getPaymentIntent());
        payment.setStatus(mapped);
        paymentRepository.save(payment);

        if (mapped == PaymentStatus.APPROVED && previous != PaymentStatus.APPROVED) {
            appointmentService.updateStatus(payment.getAppointmentId(), AppointmentStatus.COMPLETED);
            log.info("Payment {} approved, appointment {} marked as completed", payment.getId(), payment.getAppointmentId());
        }
    }

    private PaymentStatus mapStatus(Session session, String eventType) {
        if (eventType.endsWith("async_payment_failed") || eventType.endsWith("expired")) {
            return PaymentStatus.REJECTED;
        }
        if ("paid".equals(session.getPaymentStatus())) {
            return PaymentStatus.APPROVED;
        }
        return PaymentStatus.PENDING;
    }

    private long toCents(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).longValueExact();
    }
}
