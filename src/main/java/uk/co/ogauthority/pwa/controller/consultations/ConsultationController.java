package uk.co.ogauthority.pwa.controller.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationsUrlFactory;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/consultation")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ConsultationController {

  private final ConsultationRequestService consultationRequestService;
  private final ConsultationViewService consultationViewService;
  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;

  @Autowired
  public ConsultationController(
      ConsultationRequestService consultationRequestService,
      ConsultationViewService consultationViewService,
      AppProcessingBreadcrumbService appProcessingBreadcrumbService) {
    this.consultationRequestService = consultationRequestService;
    this.consultationViewService = consultationViewService;
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
  }

  //Endpoints
  @GetMapping
  public ModelAndView renderConsultations(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          AuthenticatedUserAccount authenticatedUserAccount) {
    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.CONSULTATIONS,
        () -> getConsultationModelAndView(processingContext));
  }

  @GetMapping("/withdraw/{consultationRequestId}")
  @PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.WITHDRAW_CONSULTATION})
  public ModelAndView renderWithdrawConsultation(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("consultationRequestId") Integer consultationRequestId,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         RedirectAttributes redirectAttributes) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.CONSULTATIONS,
        () -> {

          var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);
          if (!consultationRequestService.canWithDrawConsultationRequest(consultationRequest)) {
            FlashUtils.error(
                redirectAttributes, "Error", "The selected consultation request can no longer be withdrawn");
            return ReverseRouter.redirect(on(ConsultationController.class).renderConsultations(
                applicationId, pwaApplicationType, null, null));
          }
          return getWithdrawConsultationModelAndView(consultationRequest, applicationId, pwaApplicationType);

        });
  }

  @PostMapping("/withdraw/{consultationRequestId}")
  @PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.WITHDRAW_CONSULTATION})
  public ModelAndView postWithdrawConsultation(@PathVariable("applicationId") Integer applicationId,
                                                 @PathVariable("applicationType")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @PathVariable("consultationRequestId") Integer consultationRequestId,
                                                 PwaAppProcessingContext processingContext,
                                                 AuthenticatedUserAccount authenticatedUserAccount) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.CONSULTATIONS,
        () -> {

          var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);
          if (consultationRequestService.canWithDrawConsultationRequest(consultationRequest)) {
            consultationRequestService.withdrawConsultationRequest(consultationRequest, authenticatedUserAccount);
          }
          return ReverseRouter.redirect(on(ConsultationController.class).renderConsultations(
              applicationId, pwaApplicationType, null, null));

        });

  }

  //Model//Views
  private ModelAndView getConsultationModelAndView(PwaAppProcessingContext pwaAppProcessingContext) {

    var pwaApplicationDetail = pwaAppProcessingContext.getApplicationDetail();
    boolean canEditConsultations = pwaAppProcessingContext.getAppProcessingPermissions()
        .contains(PwaAppProcessingPermission.EDIT_CONSULTATIONS);

    var modelAndView = new ModelAndView("consultation/consultation")
        .addObject("requestConsultationsUrl",
            ReverseRouter.route(on(ConsultationRequestController.class).renderRequestConsultation(
                pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null, null)))
        .addObject("appRef", pwaApplicationDetail.getPwaApplicationRef())
        .addObject("consulteeGroupRequestsViews",
            consultationViewService.getConsultationRequestViews(pwaApplicationDetail.getPwaApplication()))
        .addObject("consultationsUrlFactory", new ConsultationsUrlFactory(
            pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId()))
        .addObject("caseSummaryView", pwaAppProcessingContext.getCaseSummaryView())
        .addObject("canEditConsultations", canEditConsultations);

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplicationDetail.getPwaApplication(), modelAndView,
        PwaAppProcessingTask.CONSULTATIONS.getTaskName());

    return modelAndView;

  }


  private ModelAndView getWithdrawConsultationModelAndView(ConsultationRequest consultationRequest,
                                                           Integer applicationId, PwaApplicationType pwaApplicationType) {
    return new ModelAndView("consultation/withdrawConsultation")
        .addObject("consultationRequestView",
            consultationViewService.getConsultationRequestView(consultationRequest))
        .addObject("cancelUrl", ReverseRouter.route(
          on(ConsultationController.class).renderConsultations(applicationId, pwaApplicationType, null, null)));
  }


}