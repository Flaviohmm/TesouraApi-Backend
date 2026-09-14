package com.tesoura.api.notification;

import com.tesoura.api.shared.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt <= :now")
    List<Notification> findPendingDue(@Param("status") NotificationStatus status, @Param("now") Instant now);

    @Query("SELECT n FROM Notification n WHERE n.appointmentId = :appointmentId AND n.status = :status")
    List<Notification> findByAppointmentIdAndStatus(@Param("appointmentId") UUID appointmentId, @Param("status") NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status WHERE n.appointmentId = :appointmentId AND n.status = :currentStatus")
    int cancelPendingByAppointmentId(@Param("appointmentId") UUID appointmentId, @Param("currentStatus") NotificationStatus currentStatus, @Param("status") NotificationStatus status);
}
