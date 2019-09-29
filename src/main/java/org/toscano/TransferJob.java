package org.toscano;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TransferJob implements Job {

    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        System.out.println(ctx.getJobDetail().getDescription() + "/" + ctx.getTrigger().getKey().getName() +  " fired");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new JobExecutionException(e);
        }
    }
}
