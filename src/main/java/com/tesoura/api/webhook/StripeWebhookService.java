package com.tesoura.api.webhook;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.tesoura.api.payment.Payment;
import com.tesoura.api.payment.PaymentRepository;
import com.tesoura.api.payment.PaymentService;
import com.tesoura.api.shared.tenant.TenantContext;
import com.tesoura.api.stripe.StripeAccountService;
import com.tesoura.api.stripe.StripeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StripeWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookService.class);

    private final StripeProperties properties;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final StripeAccountService stripeAccountService;
    private final ProcessedWebhookEventRepository processedWebhookEventRepository;

    public StripeWebhookService(
            StripeProperties properties,
            PaymentRepository paymentRepository,
            PaymentService paymentService,
            StripeAccountService stripeAccountService,
            ProcessedWebhookEventRepository processedWebhookEventRepository
    ) {
        this.properties = properties;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.stripeAccountService = stripeAccountService;
        this.processedWebhookEventRepository = processedWebhookEventRepository;
    }

    public Event verify(String rawBody, String signature) throws SignatureVerificationException {
        return Webhook.constructEvent(rawBody, signature, properties.webhookSigningSecret());
    }

    @Transactional
    public void process(Event event) {
        if (processedWebhookEventRepository.insertIfAbsent("stripe", event.getId()) == 0) {
            return;
        }

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        if (event.getType().startsWith("checkout.session.")) {
            com.stripe.model.StripeObject object = deserializeObject(deserializer, event.getId());
            if (!(object instanceof Session session)) {
                log.warn("Could not deserialize checkout.session object for event {}", event.getId());
                return;
            }
            Optional<Payment> payment = paymentRepository.findByExternalReference(session.getClientReferenceId());
            if (payment.isEmpty()) {
                log.warn("Stripe webhook for unknown client_reference_id={}", session.getClientReferenceId());
                return;
            }
            try {
                TenantContext.set(payment.get().getSalonId());
                paymentService.applyWebhookUpdate(session, event.getType());
            } finally {
                TenantContext.clear();
            }
            return;
        }

        if ("account.updated".equals(event.getType())) {
            com.stripe.model.StripeObject object = deserializeObject(deserializer, event.getId());
            if (object instanceof Account account) {
                stripeAccountService.refreshFromRemote(
                        account.getId(),
                        Boolean.TRUE.equals(account.getChargesEnabled()),
                        Boolean.TRUE.equals(account.getDetailsSubmitted())
                );
            }
        }
    }

    private com.stripe.model.StripeObject deserializeObject(EventDataObjectDeserializer deserializer, String eventId) {
        return deserializer.getObject().orElseGet(() -> {
            try {
                return deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException ex) {
                log.warn("Could not deserialize data object for event {}", eventId, ex);
                return null;
            }
        });
    }
}
