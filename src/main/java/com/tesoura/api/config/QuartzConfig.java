package com.tesoura.api.config;

import com.tesoura.api.notification.jobs.AppointmentReminderJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail appointmentReminderJobDetail() {
        return JobBuilder.newJob(AppointmentReminderJob.class)
                .withIdentity("appointmentReminder")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger appointmentReminderTrigger(JobDetail appointmentReminderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(appointmentReminderJobDetail)
                .withIdentity("appointmentReminderTrigger")
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(5))
                .build();
    }
}
