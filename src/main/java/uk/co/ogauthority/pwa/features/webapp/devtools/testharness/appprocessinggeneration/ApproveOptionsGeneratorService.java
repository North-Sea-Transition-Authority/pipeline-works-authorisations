package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.submission.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextParams;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.GenerateApplicationService;
import uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration.TestHarnessAppFormServiceParams;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
@Profile("test-harness")
class ApproveOptionsGeneratorService implements TestHarnessAppProcessingService {

  private final ApproveOptionsService approveOptionsService;
  private final PwaAppProcessingContextService pwaAppProcessingContextService;
  private final TaskListService taskListService;
  private final ApplicationTaskService applicationTaskService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaApplicationSubmissionService pwaApplicationSubmissionService;
  private final GenerateApplicationService generateApplicationService;
  private final ConfirmSatisfactoryGeneratorService confirmSatisfactoryGeneratorService;

  private static final PwaAppProcessingTask LINKED_APP_PROCESSING_TASK = PwaAppProcessingTask.APPROVE_OPTIONS;

  @Autowired
  public ApproveOptionsGeneratorService(
      ApproveOptionsService approveOptionsService,
      PwaAppProcessingContextService pwaAppProcessingContextService,
      TaskListService taskListService,
      ApplicationTaskService applicationTaskService,
      PwaApplicationDetailService pwaApplicationDetailService,
      PwaApplicationSubmissionService pwaApplicationSubmissionService,
      GenerateApplicationService generateApplicationService,
      ConfirmSatisfactoryGeneratorService confirmSatisfactoryGeneratorService) {
    this.approveOptionsService = approveOptionsService;
    this.pwaAppProcessingContextService = pwaAppProcessingContextService;
    this.taskListService = taskListService;
    this.applicationTaskService = applicationTaskService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaApplicationSubmissionService = pwaApplicationSubmissionService;
    this.generateApplicationService = generateApplicationService;
    this.confirmSatisfactoryGeneratorService = confirmSatisfactoryGeneratorService;
  }


  @Override
  public PwaAppProcessingTask getLinkedAppProcessingTask() {
    return LINKED_APP_PROCESSING_TASK;
  }


  @Override
  public void generateAppProcessingTaskData(TestHarnessAppProcessingProperties appProcessingProps) {

    approveOptions(appProcessingProps);
    updateAndSubmitAppForm(appProcessingProps);

    //After approving options, the new tip detail needs to be set within a new context on the
    // test harness app processing properties class for use in the upcoming tasks.
    updateContextOnTestHarnessProperties(appProcessingProps);

    confirmSatisfactoryGeneratorService.generateAppProcessingTaskData(appProcessingProps);
  }


  private void approveOptions(TestHarnessAppProcessingProperties appProcessingProps) {
    var deadlineInstant = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    approveOptionsService.approveOptions(
        appProcessingProps.getPwaApplicationDetail(), appProcessingProps.getCaseOfficerAua(), deadlineInstant);
  }



  private void updateAndSubmitAppForm(TestHarnessAppProcessingProperties appProcessingProps) {

    var tipDetail = pwaApplicationDetailService.getTipDetailByAppId(appProcessingProps.getPwaApplication().getId());

    var appFormServiceParams = new TestHarnessAppFormServiceParams(
        appProcessingProps.getApplicantAua(), tipDetail, appProcessingProps.getPipelineQuantity());

    Set<ApplicationTask> completedAppTasks = taskListService.getShownApplicationTasksForDetail(tipDetail).stream()
        .filter(appTask -> applicationTaskService.isTaskComplete(appTask, tipDetail))
        .collect(Collectors.toSet());

    //Need to do multiple passes on generating the required task form data as certain tasks only become required after others are completed
    while (true) {
      var allShownAppTasks = new HashSet<>(taskListService.getShownApplicationTasksForDetail(tipDetail));
      var uncompletedAppTasks = SetUtils.difference(allShownAppTasks, completedAppTasks);

      if (!uncompletedAppTasks.isEmpty()) {
        generateApplicationService.generateAppTasks(uncompletedAppTasks, appFormServiceParams);
        completedAppTasks.addAll(uncompletedAppTasks);
      } else {
        break;
      }
    }

    pwaApplicationSubmissionService.submitApplication(appProcessingProps.getApplicantAua(), tipDetail, null);
  }


  private void updateContextOnTestHarnessProperties(TestHarnessAppProcessingProperties appProcessingProps) {

    //applicant
    var contextParams = new PwaAppProcessingContextParams(
        appProcessingProps.getPwaApplication().getId(), appProcessingProps.getApplicantAua());
    var newContext = pwaAppProcessingContextService.validateAndCreate(contextParams);
    appProcessingProps.setApplicantProcessingContext(newContext);

    //case officer
    contextParams = new PwaAppProcessingContextParams(
        appProcessingProps.getPwaApplication().getId(), appProcessingProps.getCaseOfficerAua());
    newContext = pwaAppProcessingContextService.validateAndCreate(contextParams);
    appProcessingProps.setCaseOfficerProcessingContext(newContext);
  }



}
