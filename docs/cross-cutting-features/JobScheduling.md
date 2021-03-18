# Job Scheduling
Arbitrary tasks (jobs) can be scheduled using [Quartz](http://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/).

Quartz is configured to run in a clustered mode, where multiple Quartz instances (one per app instance) can pick up any job.

The jobs are persisted in the database and are stored 'durably' (i.e. they survive app restarts, errors, outages etc.)
Jobs will be run by a Quartz worker thread.

## Scheduling a new type of job
Jobs are executed by a `QuartzJobBean`. Logic about what to do when a job is run is defined in this bean. The bean will only execute jobs registered against its type. 
This bean can inject whatever app domain dependencies it requires.
To schedule a new type of job implement the following:

### Add a `QuartzJobBean` implementation
Create a new class which extends `QuartzJobBean`. Implement the `executeInternal` method and run whatever logic you need to do when the job is triggered.

See `uk.co.ogauthority.pwa.service.appprocessing.processingcharges.jobs.PaymentAttemptCleanupBean` for an example.

### Schedule a new job run
Create a new `JobKey` (which must be unique across all jobs). This is usually composed of an ID and an identifier.
Then create a new `JobDetail` using the bean you created earlier. 
Context data can be saved along with the job with `usingJobData`.

This `JobDetail` should then be registered with the `Scheduler` and scheduled for a particular date using `scheduler.scheduleJob()` 
or just triggered immediately with `scheduler.triggerJob(jobKey);`.

e.g
``` 
JobKey jobKey = jobKey(String.valueOf(reportRun.getId()), "Pon1MasterExtract");
JobDetail jobDetail = newJob(ReportSchedulerBean.class)
   .withIdentity(jobKey)
   .requestRecovery()
   .storeDurably()
   .build();
   
scheduler.addJob(jobDetail, false);
scheduler.triggerJob(jobKey);  
```
