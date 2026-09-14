package com.tesoura.api.notification.jobs;

import com.tesoura.api.notification.NotificationDispatchService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AppointmentReminderJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderJob.class);

    private final NotificationDispatchService notificationDispatchService;

    public AppointmentReminderJob(NotificationDispatchService notificationDispatchService) {
        this.notificationDispatchService = notificationDispatchService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing appointment reminder job");
        try {
            notificationDispatchService.processPendingNotifications();
        } catch (Exception e) {
            log.error("Error processing appointment reminders", e);
            throw new JobExecutionException(e);
        }
    }
}
