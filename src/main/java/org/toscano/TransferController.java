package org.toscano;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.UUID;

@RestController
@RequestMapping(path="/transfer")
public class TransferController {
    @Autowired
    private Scheduler scheduler;

    private JobDetail buildJobDetail(Integer id) {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("main-thread", Thread.currentThread().getName());

        return JobBuilder.newJob(TransferJob.class)
                .withIdentity(UUID.randomUUID().toString(), "transfer-jobs")
                .withDescription("transfer-" + id.toString())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "transfer-triggers")
                .withDescription("trigger-" + jobDetail.getDescription())
                .startAt(calendar.getTime())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    @PostMapping(path="/request")
    public ResponseEntity<Void> request(@RequestBody Transfer transfer) throws SchedulerException {
        JobDetail detail = buildJobDetail(transfer.getId());
        Trigger trigger = buildJobTrigger(detail);
        scheduler.scheduleJob(detail, trigger);

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
