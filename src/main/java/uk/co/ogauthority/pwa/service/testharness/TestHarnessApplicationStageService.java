package uk.co.ogauthority.pwa.service.testharness;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextParams;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.PwaAppProcessingTaskService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.testharness.appprocessinggeneration.AppProcessingTaskGeneratorService;
import uk.co.ogauthority.pwa.service.testharness.appprocessinggeneration.TestHarnessAppProcessingProperties;


/**
 *  Service to aid the test harness in pushing a pwa application through the system to a certain stage.
 */
@Service
@Profile("test-harness")
class TestHarnessApplicationStageService {

  private final PwaApplicationSubmissionService pwaApplicationSubmissionService;
  private final AppProcessingTaskGeneratorService appProcessingTaskGeneratorService;
  private final PwaAppProcessingTaskService pwaAppProcessingTaskService;
  private final PwaTeamService pwaTeamService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;
  private final PwaAppProcessingContextService pwaAppProcessingContextService;

  private static final Logger LOGGER = LoggerFactory.getLogger(TestHarnessApplicationStageService.class);

  @Autowired
  TestHarnessApplicationStageService(
      PwaApplicationSubmissionService pwaApplicationSubmissionService,
      AppProcessingTaskGeneratorService appProcessingTaskGeneratorService,
      PwaAppProcessingTaskService pwaAppProcessingTaskService,
      PwaTeamService pwaTeamService,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService,
      PwaAppProcessingContextService pwaAppProcessingContextService) {
    this.pwaApplicationSubmissionService = pwaApplicationSubmissionService;
    this.appProcessingTaskGeneratorService = appProcessingTaskGeneratorService;
    this.pwaAppProcessingTaskService = pwaAppProcessingTaskService;
    this.pwaTeamService = pwaTeamService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.pwaAppProcessingContextService = pwaAppProcessingContextService;
  }


  @Transactional
  void pushApplicationToTargetStage(PwaApplicationDetail pwaApplicationDetail,
                                    PwaApplicationStatus targetAppStatus,
                                    WebUserAccount applicantWua,
                                    Integer assignedCaseOfficerId,
                                    Integer pipelineQuantity) {


    if (PwaApplicationStatus.DRAFT.equals(targetAppStatus)) {
      throw new ActionNotAllowedException("Cannot push application to draft stage");
    }

    LOGGER.info("Pushing application to {} stage", targetAppStatus.name());
    pwaApplicationSubmissionService.submitApplication(applicantWua, pwaApplicationDetail, null);

    switch (targetAppStatus) {

      case AWAITING_APPLICATION_PAYMENT: {
        var appProcessingProps = createAppProcessingProperties(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId,
            pipelineQuantity, InitialReviewPaymentDecision.PAYMENT_REQUIRED);
        appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, List.of(PwaAppProcessingTask.INITIAL_REVIEW));
      }
      break;

      case CASE_OFFICER_REVIEW: {
        var appProcessingProps = createAppProcessingProperties(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId,
            pipelineQuantity, InitialReviewPaymentDecision.PAYMENT_WAIVED);
        appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, List.of(PwaAppProcessingTask.INITIAL_REVIEW));
      }
      break;

      case CONSENT_REVIEW: {
        var appProcessingProps = createAppProcessingProperties(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId,
            pipelineQuantity, InitialReviewPaymentDecision.PAYMENT_WAIVED);
        generateAppProcessingTasks(appProcessingProps);
      }
      break;

      case COMPLETE: {
        var appProcessingProps = createAppProcessingProperties(
            applicantWua, pwaApplicationDetail.getPwaApplication(), assignedCaseOfficerId,
            pipelineQuantity, InitialReviewPaymentDecision.PAYMENT_WAIVED);
        generateAppProcessingTasks(appProcessingProps);
        //TODO PWA-1367: issue consent
      }
      break;

      default:
        //no action required, all selectable app statuses are covered
    }


    LOGGER.info("Successfully pushed application to {} stage", targetAppStatus.name());

  }


  private void generateAppProcessingTasks(TestHarnessAppProcessingProperties appProcessingProps) {

    //Initial review task must be performed first and then create the case officer processing context once they're assigned to the app.
    //Following this, we can then perform the remaining app processing tasks with access to the case officer processing context.
    var preliminaryTask = PwaAppProcessingTask.INITIAL_REVIEW;
    appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, List.of(preliminaryTask));
    var caseOfficerProcessingContext = createAppProcessingContext(
        appProcessingProps.getCaseOfficerAua(), appProcessingProps.getPwaApplication());
    appProcessingProps.setCaseOfficerProcessingContext(caseOfficerProcessingContext);

    var remainingTasks = getShownProcessingTasksForAppContext(caseOfficerProcessingContext);
    remainingTasks.remove(preliminaryTask);

    //Test harness approach for options will not require close out task, will go to prepare consent instead
    if (appProcessingProps.getPwaApplication().getApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)) {
      remainingTasks.remove(PwaAppProcessingTask.CLOSE_OUT_OPTIONS);
    }
    appProcessingTaskGeneratorService.generateAppProcessingTasks(appProcessingProps, remainingTasks);
  }


  private TestHarnessAppProcessingProperties createAppProcessingProperties(WebUserAccount applicantWua,
                                                                           PwaApplication pwaApplication,
                                                                           Integer assignedCaseOfficerId,
                                                                           Integer pipelineQuantity,
                                                                           InitialReviewPaymentDecision paymentDecision) {

    var applicantAua = testHarnessUserRetrievalService.createAuthenticatedUserAccount(applicantWua);
    var applicantProcessingContext = createAppProcessingContext(applicantAua, pwaApplication);
    var caseOfficerAua = testHarnessUserRetrievalService.createAuthenticatedUserAccount(assignedCaseOfficerId);
    var pwaManagerPerson = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.PWA_MANAGER).stream()
        .findFirst().orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Person could not be found with %s role", PwaRegulatorRole.PWA_MANAGER.name())));
    var pwaManagerAua = testHarnessUserRetrievalService.createAuthenticatedUserAccount(pwaManagerPerson.getId().asInt());

    return new TestHarnessAppProcessingProperties(
        applicantAua, applicantProcessingContext, caseOfficerAua, pwaManagerAua, paymentDecision, pipelineQuantity);
  }



  private PwaAppProcessingContext createAppProcessingContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                             PwaApplication pwaApplication) {

    var contextParams = new PwaAppProcessingContextParams(pwaApplication.getId(), authenticatedUserAccount);
    return pwaAppProcessingContextService.validateAndCreate(contextParams);
  }

  private Set<PwaAppProcessingTask> getShownProcessingTasksForAppContext(PwaAppProcessingContext processingContext) {

    return PwaAppProcessingTask.stream()
        .filter(task -> pwaAppProcessingTaskService.canShowTask(task, processingContext))
        .collect(Collectors.toSet());
  }






}
