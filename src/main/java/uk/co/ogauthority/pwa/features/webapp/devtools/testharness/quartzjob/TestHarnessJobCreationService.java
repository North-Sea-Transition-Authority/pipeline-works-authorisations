package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.quartzjob;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;

import java.time.Instant;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.GenerateVariationApplicationForm;

@Service
@Profile("test-harness")
public class TestHarnessJobCreationService {

  private final Scheduler scheduler;
  private static final Logger LOGGER = LoggerFactory.getLogger(TestHarnessJobCreationService.class);


  @Autowired
  public TestHarnessJobCreationService(Scheduler scheduler) {
    this.scheduler = scheduler;
  }


  public void scheduleInitialGenerateApplicationJob(GenerateApplicationForm form) {

    JobKey jobKey = jobKey("TEST_HARNESS_JOB_" + Instant.now().toString());
    JobDetail jobDetail = newJob(TestHarnessBean.class)
        .withIdentity(jobKey)
        .build();

    setDefaultJobDetailProperties(jobDetail, form);
    scheduleGenerateApplicationJob(jobDetail);
  }

  public void scheduleVariationGenerateApplicationJob(GenerateVariationApplicationForm form) {

    JobKey jobKey = jobKey("TEST_HARNESS_JOB_" + Instant.now().toString());
    JobDetail jobDetail = newJob(TestHarnessBean.class)
        .withIdentity(jobKey)
        .build();

    setDefaultJobDetailProperties(jobDetail, form);
    jobDetail.getJobDataMap().put("consentedMasterPwaId", form.getConsentedMasterPwaId());
    jobDetail.getJobDataMap().put("nonConsentedMasterPwaId", form.getNonConsentedMasterPwaId());

    scheduleGenerateApplicationJob(jobDetail);
  }

  private void setDefaultJobDetailProperties(JobDetail jobDetail, GenerateApplicationForm form) {
    jobDetail.getJobDataMap().put("applicationType", form.getApplicationType());
    jobDetail.getJobDataMap().put("applicationStatus", form.getApplicationStatus());
    jobDetail.getJobDataMap().put("pipelineQuantity", form.getPipelineQuantity());
    jobDetail.getJobDataMap().put("assignedCaseOfficerId", form.getAssignedCaseOfficerId());
    jobDetail.getJobDataMap().put("applicantPersonId", form.getApplicantWuaId());
    jobDetail.getJobDataMap().put("resourceType", form.getResourceType());
  }

  private void scheduleGenerateApplicationJob(JobDetail jobDetail) {

    try {
      Trigger trigger = TriggerBuilder.newTrigger().startNow().build();
      scheduler.scheduleJob(jobDetail, trigger);
      LOGGER.info("Test harness app generation job creation complete");

    } catch (SchedulerException e) {
      throw new RuntimeException("Error scheduling test harness job", e);
    }
  }


}
