package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.applicationstage;

import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration.AppProcessingTaskGeneratorService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration.TestHarnessAppProcessingProperties;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationSubmissionService;


/**
 *  Service to aid the test harness in pushing a pwa application through the system to a certain stage.
 */
@Service
@Profile("test-harness")
public class TestHarnessApplicationStageService {

  private final PwaApplicationSubmissionService pwaApplicationSubmissionService;
  private final TestHarnessApplicationStageServiceHelper applicationStageServiceHelper;
  private final AppProcessingTaskGeneratorService appProcessingTaskGeneratorService;
  private final ConsentReviewService consentReviewService;

  private static final Logger LOGGER = LoggerFactory.getLogger(TestHarnessApplicationStageService.class);

  @Autowired
  TestHarnessApplicationStageService(
      PwaApplicationSubmissionService pwaApplicationSubmissionService,
      TestHarnessApplicationStageServiceHelper applicationStageServiceHelper,
      AppProcessingTaskGeneratorService appProcessingTaskGeneratorService1,
      ConsentReviewService consentReviewService) {
    this.pwaApplicationSubmissionService = pwaApplicationSubmissionService;
    this.applicationStageServiceHelper = applicationStageServiceHelper;
    this.appProcessingTaskGeneratorService = appProcessingTaskGeneratorService1;
    this.consentReviewService = consentReviewService;
  }


  @Transactional
  public void pushApplicationToTargetStage(PwaApplicationDetail pwaApplicationDetail,
                                           PwaApplicationStatus targetAppStatus,
                                           WebUserAccount applicantWua,
                                           Integer assignedCaseOfficerId,
                                           Integer pipelineQuantity) {


    if (PwaApplicationStatus.DRAFT.equals(targetAppStatus)) {
      throw new ActionNotAllowedException("Cannot push application to draft stage");
    }

    LOGGER.info("Pushing application to {} stage", targetAppStatus);
    pwaApplicationSubmissionService.submitApplication(applicantWua, pwaApplicationDetail, null);

    switch (targetAppStatus) {

      case AWAITING_APPLICATION_PAYMENT: {
        var appProcessingProps = applicationStageServiceHelper.createAppProcessingPropertiesPaymentRequired(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId, pipelineQuantity);
        appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, List.of(PwaAppProcessingTask.INITIAL_REVIEW));
      }
      break;

      case CASE_OFFICER_REVIEW: {
        var appProcessingProps = applicationStageServiceHelper.createAppProcessingPropertiesPaymentWaived(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId, pipelineQuantity);
        appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, List.of(PwaAppProcessingTask.INITIAL_REVIEW));
      }
      break;

      case CONSENT_REVIEW: {
        var appProcessingProps = applicationStageServiceHelper.createAppProcessingPropertiesPaymentWaived(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId, pipelineQuantity);
        generateAppProcessingTasks(appProcessingProps);
      }
      break;

      case COMPLETE: {
        var appProcessingProps = applicationStageServiceHelper.createAppProcessingPropertiesPaymentWaived(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId, pipelineQuantity);
        generateAppProcessingTasks(appProcessingProps);
        issueConsent(appProcessingProps);
      }
      break;

      default:
        //no action required, all selectable app statuses are covered
    }


    LOGGER.info("Successfully pushed application to {} stage", targetAppStatus);

  }


  private void generateAppProcessingTasks(TestHarnessAppProcessingProperties appProcessingProps) {

    //Initial review task must be performed first and then create the case officer processing context once they're assigned to the app.
    //Following this, we can then perform the remaining app processing tasks with access to the case officer processing context.
    var preliminaryTask = PwaAppProcessingTask.INITIAL_REVIEW;
    appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, List.of(preliminaryTask));
    var caseOfficerProcessingContext = applicationStageServiceHelper.createAppProcessingContext(
        appProcessingProps.getCaseOfficerAua(), appProcessingProps.getPwaApplication());
    appProcessingProps.setCaseOfficerProcessingContext(caseOfficerProcessingContext);

    var remainingTasks = applicationStageServiceHelper.getShownProcessingTasksForAppContext(caseOfficerProcessingContext);
    remainingTasks.remove(preliminaryTask);

    //Test harness approach for options will not require close out task, will go to prepare consent instead
    if (appProcessingProps.getPwaApplication().getApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)) {
      remainingTasks.remove(PwaAppProcessingTask.CLOSE_OUT_OPTIONS);
    }
    appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, remainingTasks);
  }


  private void issueConsent(TestHarnessAppProcessingProperties appProcessingProps) {
    consentReviewService.scheduleConsentIssue(
        appProcessingProps.getPwaApplicationDetail(), appProcessingProps.getPwaManagerAua());
    LOGGER.info("Scheduled consent issue for application {}",
        appProcessingProps.getPwaApplication().getAppReference());
  }



}
