package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Function;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.controller.appprocessing.AssignCaseOfficerController;
import uk.co.ogauthority.pwa.controller.appprocessing.WithdrawApplicationController;
import uk.co.ogauthority.pwa.controller.appprocessing.casenotes.CaseNoteController;
import uk.co.ogauthority.pwa.controller.appprocessing.options.ApproveOptionsController;
import uk.co.ogauthority.pwa.controller.appprocessing.options.ChangeOptionsApprovalDeadlineController;
import uk.co.ogauthority.pwa.controller.appprocessing.options.CloseOutOptionsController;
import uk.co.ogauthority.pwa.controller.consultations.ConsultationController;
import uk.co.ogauthority.pwa.controller.consultations.ConsulteeAdviceController;
import uk.co.ogauthority.pwa.controller.consultations.responses.AssignResponderController;
import uk.co.ogauthority.pwa.controller.consultations.responses.ConsultationResponseController;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeOverviewController;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.CancelPaymentRequestAppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller.CancelPaymentRequestController;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.controller.RequestApplicationUpdateController;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory.ConfirmSatisfactoryApplicationService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.confirmsatisfactory.controller.ConfirmSatisfactoryApplicationController;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.controller.InitialReviewController;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.PrepareConsentTaskService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.controller.AppConsentDocController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.application.WithdrawApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.casenotes.CaseNoteService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsTaskService;
import uk.co.ogauthority.pwa.service.appprocessing.options.ChangeOptionsApprovalDeadlineTaskService;
import uk.co.ogauthority.pwa.service.appprocessing.options.CloseOutOptionsTaskService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsulteeAdviceService;

/**
 * Enumeration of app processing-related tasks for all users.
 */
public enum PwaAppProcessingTask {

  INITIAL_REVIEW(
      "Initial review",
      TaskRequirement.REQUIRED,
      InitialReviewService.class, processingContext -> ReverseRouter.route(on(InitialReviewController.class)
      .renderInitialReview(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null,
          null, null)),
      10),

  CONFIRM_SATISFACTORY_APPLICATION(
      "Confirm satisfactory application",
      TaskRequirement.REQUIRED,
      ConfirmSatisfactoryApplicationService.class, processingContext -> ReverseRouter.route(
          on(ConfirmSatisfactoryApplicationController.class).renderConfirmSatisfactory(
              processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null,
          null, null)),
      20),

  CONSULTATIONS(
      "Consultations",
      TaskRequirement.REQUIRED,
      TaskAutoLockable.NO,
      ConsultationService.class, processingContext -> ReverseRouter.route(on(ConsultationController.class)
      .renderConsultations(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null,
          null)),
      40),

  APPROVE_OPTIONS(
      "Approve options",
      TaskRequirement.REQUIRED,
      ApproveOptionsTaskService.class, processingContext -> ReverseRouter.route(on(ApproveOptionsController.class)
      .renderApproveOptions(processingContext.getMasterPwaApplicationId(),
          processingContext.getApplicationType(), null, null, null)),
      45),

  CLOSE_OUT_OPTIONS(
      "Close options application without consent",
      TaskRequirement.REQUIRED,
      CloseOutOptionsTaskService.class, processingContext -> ReverseRouter.route(on(CloseOutOptionsController.class)
      .renderCloseOutOptions(processingContext.getMasterPwaApplicationId(),
          processingContext.getApplicationType(), null, null)),
      46),

  CHANGE_OPTIONS_APPROVAL_DEADLINE(
      "Change options approval deadline",
      TaskRequirement.OPTIONAL,
      ChangeOptionsApprovalDeadlineTaskService.class, processingContext ->
      ReverseRouter.route(on(ChangeOptionsApprovalDeadlineController.class)
          .renderChangeDeadline(processingContext.getMasterPwaApplicationId(),
              processingContext.getApplicationType(), null, null, null)),
      47
  ),

  PUBLIC_NOTICE(
      "Public notice",
      TaskRequirement.REQUIRED,
      TaskAutoLockable.NO,
      PublicNoticeService.class, processingContext ->  ReverseRouter.route(on(PublicNoticeOverviewController.class)
      .renderPublicNoticeOverview(processingContext.getMasterPwaApplicationId(),
          processingContext.getApplicationType(), null, null)),
      50),

  PREPARE_CONSENT(
      "Prepare consent",
      TaskRequirement.REQUIRED,
      TaskAutoLockable.NO,
      PrepareConsentTaskService.class, processingContext -> ReverseRouter.route(on(AppConsentDocController.class)
      .renderConsentDocEditor(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(),
          null, null)),
      60),

  ALLOCATE_RESPONDER(
      "Allocate responder",
      TaskRequirement.REQUIRED,
      AssignResponderService.class, processingContext -> ReverseRouter.route(on(AssignResponderController.class)
      .renderAssignResponder(processingContext.getMasterPwaApplicationId(),
          processingContext.getApplicationType(), processingContext.getActiveConsultationRequestId(), null, null)),
      70),

  CONSULTEE_ADVICE(
      "View consultations",
      TaskRequirement.OPTIONAL,
      ConsulteeAdviceService.class, processingContext -> ReverseRouter.route(on(ConsulteeAdviceController.class)
      .renderConsulteeAdvice(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(),
          null)),
      75
  ),

  CONSULTATION_RESPONSE(
      "Provide response",
      TaskRequirement.REQUIRED,
      ConsultationResponseService.class,      processingContext -> ReverseRouter.route(on(ConsultationResponseController.class)
          .renderResponder(
              processingContext.getMasterPwaApplicationId(),
              processingContext.getApplicationType(), processingContext.getActiveConsultationRequestId(), null, null
          )),
      80),

  ALLOCATE_CASE_OFFICER(
      "Allocate case officer",
      TaskRequirement.OPTIONAL,
      AssignCaseOfficerService.class, processingContext -> ReverseRouter.route(on(AssignCaseOfficerController.class)
      .renderAssignCaseOfficer(processingContext.getMasterPwaApplicationId(),
          processingContext.getApplicationType(), null, null, null)),
      90),

  RFI(
      "Request further information",
      TaskRequirement.OPTIONAL,
      ApplicationUpdateRequestService.class, processingContext -> ReverseRouter.route(on(RequestApplicationUpdateController.class)
          .renderRequestUpdate(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(),
              null, null, null)),
      100),

  ADD_NOTE_OR_DOCUMENT(
      "Add note/document",
      TaskRequirement.OPTIONAL,
      TaskAutoLockable.NO,
      CaseNoteService.class, processingContext -> ReverseRouter.route(on(CaseNoteController.class)
      .renderAddCaseNote(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null,
          null, null)),
      110),

  CANCEL_PAYMENT(
      "Cancel payment request",
      TaskRequirement.OPTIONAL,
      CancelPaymentRequestAppProcessingService.class, processingContext -> ReverseRouter.route(on(CancelPaymentRequestController.class)
      .renderCancelPaymentRequest(processingContext.getMasterPwaApplicationId(),
          processingContext.getApplicationType(), null, null)),
      115),

  WITHDRAW_APPLICATION(
      "Withdraw application",
      TaskRequirement.OPTIONAL,
      WithdrawApplicationService.class, processingContext -> ReverseRouter.route(on(WithdrawApplicationController.class)
      .renderWithdrawApplication(processingContext.getMasterPwaApplicationId(),
          processingContext.getApplicationType(), null, null, null)),
      120);

  private final String taskName;
  private final TaskRequirement taskRequirement;
  private final TaskAutoLockable taskAutoLockable;
  private final Class<? extends AppProcessingService> serviceClass;
  private final Function<PwaAppProcessingContext, String> routeFunction;
  private final int displayOrder;

  PwaAppProcessingTask(String taskName,
                       TaskRequirement taskRequirement,
                       TaskAutoLockable taskAutoLockable,
                       Class<? extends AppProcessingService> serviceClass,
                       Function<PwaAppProcessingContext, String> routeFunction,
                       int displayOrder) {
    this.taskName = taskName;
    this.taskRequirement = taskRequirement;
    this.taskAutoLockable = taskAutoLockable;
    this.serviceClass = serviceClass;
    this.routeFunction = routeFunction;
    this.displayOrder = displayOrder;
  }

  PwaAppProcessingTask(String taskName,
                       TaskRequirement taskRequirement,
                       Class<? extends AppProcessingService> serviceClass,
                       Function<PwaAppProcessingContext, String> routeFunction,
                       int displayOrder) {
    this.taskName = taskName;
    this.taskRequirement = taskRequirement;
    taskAutoLockable = TaskAutoLockable.YES;
    this.serviceClass = serviceClass;
    this.routeFunction = routeFunction;
    this.displayOrder = displayOrder;
  }

  public String getTaskName() {
    return taskName;
  }

  public TaskRequirement getTaskRequirement() {
    return taskRequirement;
  }

  public TaskAutoLockable getTaskAutoLockable() {
    return taskAutoLockable;
  }

  public Class<? extends AppProcessingService> getServiceClass() {
    return serviceClass;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<PwaAppProcessingTask> stream() {
    return Stream.of(PwaAppProcessingTask.values());
  }

  public String getRoute(PwaAppProcessingContext processingContext) {
    return routeFunction.apply(processingContext);
  }

  public static PwaAppProcessingTask resolveFromTaskName(String taskName) {
    return PwaAppProcessingTask.stream()
        .filter(task -> task.getTaskName().equals(taskName))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format(
            "Couldn't resolve PwaAppProcessingTask from taskName: [%s]", taskName)));
  }

}
