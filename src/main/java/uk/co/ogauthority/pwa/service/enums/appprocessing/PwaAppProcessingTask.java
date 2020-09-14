package uk.co.ogauthority.pwa.service.enums.appprocessing;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.controller.appprocessing.AssignCaseOfficerController;
import uk.co.ogauthority.pwa.controller.appprocessing.applicationupdate.RequestApplicationUpdateController;
import uk.co.ogauthority.pwa.controller.appprocessing.casenotes.CaseNoteController;
import uk.co.ogauthority.pwa.controller.appprocessing.decision.AppConsentDocController;
import uk.co.ogauthority.pwa.controller.appprocessing.initialreview.InitialReviewController;
import uk.co.ogauthority.pwa.controller.consultations.ConsultationController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.CaseSetupService;
import uk.co.ogauthority.pwa.service.appprocessing.PublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.application.AcceptApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.application.WithdrawApplicationService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.casenotes.CaseNoteService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationAdviceService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.ConsultationService;
import uk.co.ogauthority.pwa.service.appprocessing.decision.ApplicationDecisionService;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;

/**
 * Enumeration of app processing-related tasks for all users.
 */
public enum PwaAppProcessingTask {

  INITIAL_REVIEW(
      "Initial review",
      TaskRequirement.REQUIRED,
      InitialReviewService.class,
      10) {

    public String getRoute(PwaApplicationDetail detail) {

      return ReverseRouter.route(on(InitialReviewController.class)
          .renderInitialReview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null, null));

    }

  },

  ACCEPT_APPLICATION(
      "Accept application",
      TaskRequirement.REQUIRED,
      AcceptApplicationService.class,
      20) {

    public String getRoute(PwaApplicationDetail detail) {

      return "#";

    }

  },

  CASE_SETUP(
      "Case setup",
      TaskRequirement.REQUIRED,
      CaseSetupService.class,
      30) {

    public String getRoute(PwaApplicationDetail detail) {

      return "#";

    }

  },

  CONSULTATIONS(
      "Consultations",
      TaskRequirement.REQUIRED,
      ConsultationService.class,
      40) {

    public String getRoute(PwaApplicationDetail detail) {

      return ReverseRouter.route(on(ConsultationController.class)
          .renderConsultations(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null));

    }

  },

  PUBLIC_NOTICE(
      "Public notice",
      TaskRequirement.REQUIRED,
      PublicNoticeService.class,
      50) {

    public String getRoute(PwaApplicationDetail detail) {

      return "#";

    }

  },

  DECISION(
      "Decision",
      TaskRequirement.REQUIRED,
      ApplicationDecisionService.class,
      60) {

    public String getRoute(PwaApplicationDetail detail) {

      return ReverseRouter.route(on(AppConsentDocController.class)
          .renderConsentDocEditor(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null));

    }

  },

  ALLOCATE_RESPONDER(
      "Allocate responder",
      TaskRequirement.REQUIRED,
      AssignResponderService.class,
      70) {

    public String getRoute(PwaApplicationDetail detail) {

      return "#";

    }

  },

  CONSULTATION_ADVICE(
      "Consultation advice",
      TaskRequirement.REQUIRED,
      ConsultationAdviceService.class,
      80) {

    public String getRoute(PwaApplicationDetail detail) {

      return "#";

    }

  },

  ALLOCATE_CASE_OFFICER(
      "Allocate case officer",
      TaskRequirement.OPTIONAL,
      AssignCaseOfficerService.class,
      90) {

    public String getRoute(PwaApplicationDetail detail) {

      return ReverseRouter.route(on(AssignCaseOfficerController.class)
          .renderAssignCaseOfficer(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null, null));

    }

  },

  RFI(
      "Request further information",
      TaskRequirement.OPTIONAL,
      ApplicationUpdateRequestService.class,
      100) {

    public String getRoute(PwaApplicationDetail detail) {

      return ReverseRouter.route(on(RequestApplicationUpdateController.class)
          .renderRequestUpdate(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null, null));

    }

  },

  ADD_NOTE_OR_DOCUMENT(
      "Add note/document",
      TaskRequirement.OPTIONAL,
      CaseNoteService.class,
      110) {

    public String getRoute(PwaApplicationDetail detail) {

      return ReverseRouter.route(on(CaseNoteController.class)
          .renderAddCaseNote(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null, null));

    }

  },

  WITHDRAW_APPLICATION(
      "Withdraw application",
      TaskRequirement.OPTIONAL,
      WithdrawApplicationService.class,
      120) {

    public String getRoute(PwaApplicationDetail detail) {

      return "#";

    }

  };

  private final String taskName;
  private final TaskRequirement taskRequirement;
  private final Class<? extends AppProcessingService> serviceClass;
  private final int displayOrder;

  PwaAppProcessingTask(String taskName,
                       TaskRequirement taskRequirement,
                       Class<? extends AppProcessingService> serviceClass, int displayOrder) {
    this.taskName = taskName;
    this.taskRequirement = taskRequirement;
    this.serviceClass = serviceClass;
    this.displayOrder = displayOrder;
  }

  public String getTaskName() {
    return taskName;
  }

  public TaskRequirement getTaskRequirement() {
    return taskRequirement;
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

  public abstract String getRoute(PwaApplicationDetail detail);

}
