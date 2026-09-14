package com.tesoura.api.notification;

import com.tesoura.api.appointment.Appointment;
import com.tesoura.api.shared.enums.NotificationChannel;
import com.tesoura.api.shared.enums.NotificationStatus;
import com.tesoura.api.shared.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationRepository notificationRepository;

    public NotificationDispatchService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void scheduleForAppointment(Appointment appointment) {
        log.info("Scheduling notifications for appointment {}", appointment.getId());

        Notification reminder = new Notification();
        reminder.setSalonId(appointment.getSalonId());
        reminder.setAppointmentId(appointment.getId());
        reminder.setChannel(NotificationChannel.WHATSAPP);
        reminder.setStatus(NotificationStatus.PENDING);
        reminder.setRecipient("client-phone-placeholder");
        reminder.setMessage("Lembrete: Agendamento em " + appointment.getStartsAt());
        reminder.setScheduledAt(appointment.getStartsAt().minusSeconds(3600));
        notificationRepository.save(reminder);
    }

    @Transactional
    public void cancelPendingForAppointment(UUID appointmentId) {
        log.info("Cancelling pending notifications for appointment {}", appointmentId);
        notificationRepository.cancelPendingByAppointmentId(
                appointmentId,
                NotificationStatus.PENDING,
                NotificationStatus.FAILED
        );
    }

    @Transactional
    public void processPendingNotifications() {
        List<Notification> pending = notificationRepository.findPendingDue(NotificationStatus.PENDING, Instant.now());
        for (Notification notification : pending) {
            try {
                log.info("Sending notification {} to {}", notification.getId(), notification.getRecipient());
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(Instant.now());
                notificationRepository.save(notification);
            } catch (Exception e) {
                log.error("Failed to send notification {}", notification.getId(), e);
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }
}
