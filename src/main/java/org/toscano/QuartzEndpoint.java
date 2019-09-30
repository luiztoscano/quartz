package org.toscano;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Endpoint(id = "quartz")
public class QuartzEndpoint {

    @Autowired
    private Scheduler scheduler;

    @ReadOperation
    public List<String> scheduledJobs() throws SchedulerException {
        List<String> jobs = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();
                String jobDescription = scheduler.getJobDetail(jobKey).getDescription();

                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);

                Date nextFireTime = null;
                Date lastFireTime = null;

                if (triggers.size() > 0) {
                    nextFireTime = triggers.get(0).getNextFireTime();
                    lastFireTime = triggers.get(0).getPreviousFireTime();
                }

                String job = "[jobDescription]: " + jobDescription +
                        " - [jobName] : " + jobName +
                        " - [groupName] : " + jobGroup +
                        " - [lastFireTime] : " + lastFireTime +
                        " - [nextFireTime] : " + nextFireTime;

                jobs.add(job);
            }
        }

        return jobs;
    }
}
