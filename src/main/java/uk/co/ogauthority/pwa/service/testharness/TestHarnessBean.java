package uk.co.ogauthority.pwa.service.testharness;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class TestHarnessBean extends QuartzJobBean {

  private final TestHarnessService testHarnessService;

  @Autowired
  public TestHarnessBean(TestHarnessService testHarnessService) {
    this.testHarnessService = testHarnessService;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {

      JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

      testHarnessService.generatePwaApplication(
          (PwaApplicationType) jobDataMap.get("applicationType"),
          (PwaApplicationStatus) jobDataMap.get("applicationStatus"),
          (Integer) jobDataMap.get("pipelineQuantity"),
          (Integer) jobDataMap.get("assignedCaseOfficerId"),
          (Integer) jobDataMap.get("applicantPersonId"));

    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
