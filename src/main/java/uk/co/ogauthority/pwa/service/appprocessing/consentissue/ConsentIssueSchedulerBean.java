package uk.co.ogauthority.pwa.service.appprocessing.consentissue;

import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentIssueException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

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

  /**
   * Note: Not using Transactional annotation here as the individual methods define their own transactions to
   * commit/rollback success or failure independently.
   */
  @Override
  protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {

    try {

      String jobKeyName = context.getJobDetail().getKey().getName();
      String pwaApplicationDetailIdString = StringUtils.substringBefore(jobKeyName, "-");
      var detail = pwaApplicationDetailService.getDetailById(Integer.valueOf(pwaApplicationDetailIdString));

      int issuingWuaId = context.getJobDetail().getJobDataMap().getInt("issuingWuaId");
      var approvalTime = (Instant) context.getJobDetail().getJobDataMap().get("approvalTime");
      var issuingUser = userAccountService.getWebUserAccount(issuingWuaId);

      LOGGER.info("Executing consent issue job for PAD with id {}... [isRecovering = {}]", detail.getId(), context.isRecovering());

      try {
        consentIssueService.issueConsent(detail, issuingUser, approvalTime);
      } catch (Exception e) {
        consentIssueService.failConsentIssue(detail, e, issuingUser);
        throw new ConsentIssueException(String.format("Error issuing consent for PAD with id: %s", detail.getId()), e);
      }

      LOGGER.info("Consent issue complete for PAD with id {}", detail.getId());

    } catch (Exception e) {
      LOGGER.error("Consent issue job execution failed", e);
      throw new JobExecutionException(e);
    }
  }

}