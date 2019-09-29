package org.toscano;

import net.joelinn.quartz.jobstore.RedisJobStore;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Calendar;

@RestController
@RequestMapping(path="/transfer")
public class TransferController {
    @Autowired
    private Scheduler scheduler;

    @Bean
    public Scheduler getScheduler() throws SchedulerException {
        SchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();
        System.out.println("Scheduler name is: " + scheduler.getSchedulerName());
        System.out.println("Scheduler instance ID is: " + scheduler.getSchedulerInstanceId());
        System.out.println("Scheduler context's value for key QuartzTopic is " + scheduler.getContext().getString("QuartzTopic"));
        scheduler.start();

        return scheduler;
    }

    @Autowired
    private JobStore store;

    @Bean
    public JobStore getJobStore() {
        return new RedisJobStore();
    }

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
//        System.out.println(transfer.getId() + " scheduled");

        return new ResponseEntity<Void>(HttpStatus.OK);
    }


    @GetMapping(path = "jobs")
    public ResponseEntity<List<String>> getAllJobs() throws SchedulerException {
        List<String> jobs = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);

                Date nextFireTime = null;
                Date lastFireTime = null;

                if (triggers.size() > 0) {
                    nextFireTime = triggers.get(0).getNextFireTime();
                    lastFireTime = triggers.get(0).getPreviousFireTime();
                }

                String job = "[jobName] : " + jobName +
                        " - [groupName] : " + jobGroup +
                        " - [lastFireTime] : " + lastFireTime +
                        " - [nextFireTime] : " + nextFireTime;

                jobs.add(job);

            }

        }

        return new ResponseEntity<List<String>>(jobs, HttpStatus.OK);
    }



}
