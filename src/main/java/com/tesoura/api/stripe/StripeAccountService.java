package com.tesoura.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.param.AccountCreateParams;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;
import com.tesoura.api.shared.exception.ApiException;
import com.tesoura.api.stripe.dto.StripeAccountStatusDTO;
import com.tesoura.api.stripe.dto.StripeAuthorizationUrlDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class StripeAccountService {

    private static final Logger log = LoggerFactory.getLogger(StripeAccountService.class);

    private final StripeAccountRepository accountRepository;
    private final StripeProperties properties;

    public StripeAccountService(StripeAccountRepository accountRepository, StripeProperties properties) {
        this.accountRepository = accountRepository;
        this.properties = properties;
    }

    @Transactional
    public StripeAuthorizationUrlDTO createOnboardingLink(UUID salonId) {
        StripeAccount account = accountRepository.findBySalonId(salonId).orElse(null);
        try {
            if (account == null) {
                Account remoteAccount = Account.create(AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setCountry("BR")
                        .setCapabilities(AccountCreateParams.Capabilities.builder()
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                        .setRequested(true).build())
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                        .setRequested(true).build())
                                .setPixPayments(AccountCreateParams.Capabilities.PixPayments.builder()
                                        .setRequested(true).build())
                                .setBoletoPayments(AccountCreateParams.Capabilities.BoletoPayments.builder()
                                        .setRequested(true).build())
                                .build())
                        .build());

                account = new StripeAccount();
                account.setSalonId(salonId);
                account.setAccountId(remoteAccount.getId());
                accountRepository.save(account);
            }

            AccountLink link = AccountLink.create(AccountLinkCreateParams.builder()
                    .setAccount(account.getAccountId())
                    .setRefreshUrl(properties.frontendReturnUrl() + "/configuracoes?stripe=refresh")
                    .setReturnUrl(properties.frontendReturnUrl() + "/configuracoes?stripe=return")
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build());

            return new StripeAuthorizationUrlDTO(link.getUrl());
        } catch (StripeException ex) {
            log.error("Failed to create Stripe onboarding link", ex);
            throw ApiException.badRequest("Não foi possível iniciar a conexão com o Stripe");
        }
    }

    @Transactional
    public StripeAccountStatusDTO getStatus(UUID salonId) {
        StripeAccount account = accountRepository.findBySalonId(salonId).orElse(null);
        if (account == null) {
            return StripeAccountStatusDTO.disconnected();
        }
        if (!account.isChargesEnabled()) {
            refresh(account);
        }
        return new StripeAccountStatusDTO(true, account.isChargesEnabled(), account.getAccountId());
    }

    @Transactional
    public void refreshFromRemote(String accountId, boolean chargesEnabled, boolean detailsSubmitted) {
        accountRepository.findByAccountId(accountId).ifPresent(acc -> {
            acc.setChargesEnabled(chargesEnabled);
            acc.setDetailsSubmitted(detailsSubmitted);
            acc.setStatus(chargesEnabled ? "connected" : "pending");
            acc.setUpdatedAt(Instant.now());
            accountRepository.save(acc);
        });
    }

    private void refresh(StripeAccount account) {
        try {
            Account remoteAccount = Account.retrieve(account.getAccountId());
            account.setChargesEnabled(Boolean.TRUE.equals(remoteAccount.getChargesEnabled()));
            account.setDetailsSubmitted(Boolean.TRUE.equals(remoteAccount.getDetailsSubmitted()));
            account.setStatus(account.isChargesEnabled() ? "connected" : "pending");
            account.setUpdatedAt(Instant.now());
            accountRepository.save(account);
        } catch (StripeException ex) {
            log.warn("Failed to refresh Stripe account status for {}", account.getAccountId(), ex);
        }
    }
}
