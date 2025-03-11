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
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;


/**
 *  Service to aid the test harness stage service in creating the data needed to begin pushing the app through the system.
 */
@Service
@Profile("test-harness")
class TestHarnessApplicationStageServiceHelper {

  private final PwaAppProcessingTaskService pwaAppProcessingTaskService;
  private final TestHarnessUserRetrievalService testHarnessUserRetrievalService;
  private final PwaAppProcessingContextService pwaAppProcessingContextService;
  private final TeamQueryService teamQueryService;


  @Autowired
  TestHarnessApplicationStageServiceHelper(
      PwaAppProcessingTaskService pwaAppProcessingTaskService,
      TestHarnessUserRetrievalService testHarnessUserRetrievalService,
      PwaAppProcessingContextService pwaAppProcessingContextService, TeamQueryService teamQueryService) {
    this.pwaAppProcessingTaskService = pwaAppProcessingTaskService;
    this.testHarnessUserRetrievalService = testHarnessUserRetrievalService;
    this.pwaAppProcessingContextService = pwaAppProcessingContextService;
    this.teamQueryService = teamQueryService;
  }

  private TestHarnessAppProcessingProperties createAppProcessingProperties(WebUserAccount applicantWua,
                                                                   PwaApplication pwaApplication,
                                                                   Integer assignedCaseOfficerId,
                                                                   Integer pipelineQuantity,
                                                                   InitialReviewPaymentDecision paymentDecision) {

    var applicantAua = testHarnessUserRetrievalService.createAuthenticatedUserAccount(applicantWua);
    var applicantProcessingContext = createAppProcessingContext(applicantAua, pwaApplication);
    var caseOfficerAua = testHarnessUserRetrievalService.createAuthenticatedUserAccount(assignedCaseOfficerId);
    var pwaManagerPerson = teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, Role.PWA_MANAGER).stream()
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Person could not be found with %s role".formatted(Role.PWA_MANAGER.name())));
    var pwaManagerAua = testHarnessUserRetrievalService.createAuthenticatedUserAccount(Math.toIntExact((pwaManagerPerson.wuaId())));

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