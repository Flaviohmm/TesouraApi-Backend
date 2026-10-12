package com.tesoura.api.publicapi;

import com.tesoura.api.appointment.AppointmentRepository;
import com.tesoura.api.appointment.dto.AppointmentResponseDTO;
import com.tesoura.api.customer.CustomerContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/customers/me")
public class CustomerBookingsController {

    private final AppointmentRepository appointmentRepository;

    public CustomerBookingsController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/appointments")
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> myAppointments() {
        var customerId = CustomerContext.require().customerId();
        return appointmentRepository.findByClientCustomerId(customerId).stream()
                .map(AppointmentResponseDTO::from)
                .toList();
    }
}
