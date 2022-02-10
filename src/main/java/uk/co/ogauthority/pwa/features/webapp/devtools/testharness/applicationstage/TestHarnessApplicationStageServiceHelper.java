package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.applicationstage;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextParams;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTaskService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.TestHarnessUserRetrievalService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration.TestHarnessAppProcessingProperties;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;


/**
 *  Service to aid the test harness stage service in creating the data needed to begin pushing the app through the system.
 */
@Service
@Profile("test-harness")
class TestHarnessApplicationStageServiceHelper {

  private final PwaAppProcessingTaskService pwaAppProcessingTaskService;
  private final PwaTeamService pwaTeamService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;
  private final PwaAppProcessingContextService pwaAppProcessingContextService;


  @Autowired
  TestHarnessApplicationStageServiceHelper(
      PwaAppProcessingTaskService pwaAppProcessingTaskService,
      PwaTeamService pwaTeamService,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService,
      PwaAppProcessingContextService pwaAppProcessingContextService) {
    this.pwaAppProcessingTaskService = pwaAppProcessingTaskService;
    this.pwaTeamService = pwaTeamService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.pwaAppProcessingContextService = pwaAppProcessingContextService;
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

  TestHarnessAppProcessingProperties createAppProcessingPropertiesPaymentRequired(WebUserAccount applicantWua,
                                                                                 PwaApplication pwaApplication,
                                                                                 Integer assignedCaseOfficerId,
                                                                                 Integer pipelineQuantity) {
    return createAppProcessingProperties(
        applicantWua, pwaApplication, assignedCaseOfficerId, pipelineQuantity, InitialReviewPaymentDecision.PAYMENT_REQUIRED);
  }

  TestHarnessAppProcessingProperties createAppProcessingPropertiesPaymentWaived(WebUserAccount applicantWua,
                                                                                 PwaApplication pwaApplication,
                                                                                 Integer assignedCaseOfficerId,
                                                                                 Integer pipelineQuantity) {
    return createAppProcessingProperties(
        applicantWua, pwaApplication, assignedCaseOfficerId, pipelineQuantity, InitialReviewPaymentDecision.PAYMENT_WAIVED);
  }



  PwaAppProcessingContext createAppProcessingContext(AuthenticatedUserAccount authenticatedUserAccount,
                                                     PwaApplication pwaApplication) {

    var contextParams = new PwaAppProcessingContextParams(pwaApplication.getId(), authenticatedUserAccount);
    return pwaAppProcessingContextService.validateAndCreate(contextParams);
  }

  Set<PwaAppProcessingTask> getShownProcessingTasksForAppContext(PwaAppProcessingContext processingContext) {

    return PwaAppProcessingTask.stream()
        .filter(task -> pwaAppProcessingTaskService.canShowTask(task, processingContext))
        .collect(Collectors.toSet());
  }






}
