package com.tesoura.api.publicapi;

import com.tesoura.api.appointment.AppointmentService;
import com.tesoura.api.appointment.dto.AppointmentRequestDTO;
import com.tesoura.api.appointment.dto.AppointmentResponseDTO;
import com.tesoura.api.client.Client;
import com.tesoura.api.client.ClientRepository;
import com.tesoura.api.customer.Customer;
import com.tesoura.api.customer.CustomerContext;
import com.tesoura.api.customer.CustomerPrincipal;
import com.tesoura.api.customer.CustomerRepository;
import com.tesoura.api.professional.Professional;
import com.tesoura.api.professional.ProfessionalRepository;
import com.tesoura.api.publicapi.dto.PublicAppointmentRequestDTO;
import com.tesoura.api.publicapi.dto.PublicProfessionalDTO;
import com.tesoura.api.publicapi.dto.PublicSalonDetailDTO;
import com.tesoura.api.publicapi.dto.PublicSalonSummaryDTO;
import com.tesoura.api.publicapi.dto.PublicServiceDTO;
import com.tesoura.api.salon.Salon;
import com.tesoura.api.salon.SalonRepository;
import com.tesoura.api.service_catalog.HairService;
import com.tesoura.api.service_catalog.HairServiceRepository;
import com.tesoura.api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicSalonService {

    private static final String ACTIVE_STATUS = "active";

    private final SalonRepository salonRepository;
    private final HairServiceRepository hairServiceRepository;
    private final ProfessionalRepository professionalRepository;
    private final ClientRepository clientRepository;
    private final CustomerRepository customerRepository;
    private final AppointmentService appointmentService;

    public PublicSalonService(
            SalonRepository salonRepository,
            HairServiceRepository hairServiceRepository,
            ProfessionalRepository professionalRepository,
            ClientRepository clientRepository,
            CustomerRepository customerRepository,
            AppointmentService appointmentService
    ) {
        this.salonRepository = salonRepository;
        this.hairServiceRepository = hairServiceRepository;
        this.professionalRepository = professionalRepository;
        this.clientRepository = clientRepository;
        this.customerRepository = customerRepository;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    public List<PublicSalonSummaryDTO> listSalons() {
        return salonRepository.findBySubscriptionStatusOrderByNameAsc(ACTIVE_STATUS).stream()
                .map(PublicSalonSummaryDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicSalonDetailDTO getDetail(String slug) {
        Salon salon = requireSalon(slug);
        List<PublicServiceDTO> services = hairServiceRepository.findBySalonIdOrderByNameAsc(salon.getId()).stream()
                .filter(HairService::isActive)
                .map(PublicServiceDTO::from)
                .toList();
        List<PublicProfessionalDTO> professionals = professionalRepository.findBySalonIdOrderByNameAsc(salon.getId()).stream()
                .filter(Professional::isActive)
                .map(PublicProfessionalDTO::from)
                .toList();
        return PublicSalonDetailDTO.from(salon, services, professionals);
    }

    @Transactional
    public AppointmentResponseDTO book(String slug, PublicAppointmentRequestDTO request) {
        Salon salon = requireSalon(slug);
        CustomerPrincipal principal = CustomerContext.require();
        Customer customer = customerRepository.findById(principal.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        Client client = resolveClientForCustomer(salon, customer);

        AppointmentRequestDTO appointmentRequest = new AppointmentRequestDTO(
                client.getId(),
                request.professionalId(),
                request.startsAt(),
                request.serviceIds(),
                request.notes(),
                "marketplace",
                null,
                null,
                null
        );
        return appointmentService.create(appointmentRequest);
    }

    private Client resolveClientForCustomer(Salon salon, Customer customer) {
        Client client = clientRepository.findBySalonIdAndPhone(salon.getId(), customer.getPhone())
                .orElseGet(() -> {
                    Client created = new Client();
                    created.setSalonId(salon.getId());
                    created.setName(customer.getName());
                    created.setPhone(customer.getPhone());
                    created.setEmail(customer.getEmail());
                    return created;
                });
        if (client.getCustomerId() == null) {
            client.setCustomerId(customer.getId());
        }
        return clientRepository.save(client);
    }

    private Salon requireSalon(String slug) {
        return salonRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Salão não encontrado"));
    }
}
