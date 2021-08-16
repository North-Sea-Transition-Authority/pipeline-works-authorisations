package uk.co.ogauthority.pwa.service.appprocessing.consentissue;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentIssueException;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.users.UserAccountService;

/**
 * Handles any job executions registered against this bean.
 */
@Component
public class ConsentIssueSchedulerBean extends QuartzJobBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentIssueSchedulerBean.class);
  private final ConsentIssueService consentIssueService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final UserAccountService userAccountService;

  @Autowired
  public ConsentIssueSchedulerBean(ConsentIssueService consentIssueService,
                                   PwaApplicationDetailService pwaApplicationDetailService,
                                   UserAccountService userAccountService) {
    this.consentIssueService = consentIssueService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.userAccountService = userAccountService;
  }

  @Override
  protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {

    try {

      String pwaApplicationDetailId = context.getJobDetail().getKey().getName();
      var detail = pwaApplicationDetailService.getDetailById(Integer.valueOf(pwaApplicationDetailId));

      int issuingWuaId = context.getJobDetail().getJobDataMap().getInt("issuingWuaId");
      var issuingUser = userAccountService.getWebUserAccount(issuingWuaId);

      LOGGER.info("Executing consent issue job for PAD with id {}... [isRecovering = {}]", detail.getId(), context.isRecovering());

      try {
        consentIssueService.issueConsent(detail, issuingUser);
      } catch (Exception e) {
        // todo PWA-1425 what do we do when it fails?
        throw new ConsentIssueException(String.format("Error issuing consent for PAD with id: %s", detail.getId()), e);
      }

      LOGGER.info("Consent issue complete for PAD with id {}", detail.getId());

    } catch (Exception e) {
      LOGGER.error("Consent issue job execution failed", e);
      // todo PWA-1425 what do we do when it fails?
      throw new JobExecutionException(e);
    }
  }

}