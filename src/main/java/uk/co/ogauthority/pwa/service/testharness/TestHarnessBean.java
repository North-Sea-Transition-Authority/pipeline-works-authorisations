package uk.co.ogauthority.pwa.service.testharness;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.users.UserAccountService;

@Profile("development")
class TestHarnessBean extends QuartzJobBean {

  private final TestHarnessService testHarnessService;
  private final UserAccountService userAccountService;

  @Autowired
  public TestHarnessBean(TestHarnessService testHarnessService,
                         UserAccountService userAccountService) {
    this.testHarnessService = testHarnessService;
    this.userAccountService = userAccountService;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {

      // some functionality relies of hibernate envers, and that uses the security context to associate changes to users.
      // we need to manually set the default system user as the security context to account for this.
      var systemWua = userAccountService.getSystemWebUserAccount();
      Authentication auth = new UsernamePasswordAuthenticationToken(systemWua, null);
      SecurityContextHolder.getContext().setAuthentication(auth);


      JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
      testHarnessService.generatePwaApplication(
          (PwaApplicationType) jobDataMap.get("applicationType"),
          (PwaApplicationStatus) jobDataMap.get("applicationStatus"),
          (Integer) jobDataMap.get("pipelineQuantity"),
          (Integer) jobDataMap.get("assignedCaseOfficerId"),
          (Integer) jobDataMap.get("applicantPersonId"));

    } catch (Exception e) {
      throw new JobExecutionException(e);
    } finally {
      SecurityContextHolder.clearContext();
    }

  }
}
