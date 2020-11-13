package uk.co.ogauthority.pwa.controller.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/request-consultation")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.EDIT_CONSULTATIONS})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ConsultationRequestController {

  private final ConsultationRequestService consultationRequestService;
  private final ControllerHelperService controllerHelperService;
  private final AppProcessingBreadcrumbService breadcrumbService;

  @Autowired
  public ConsultationRequestController(ConsultationRequestService consultationRequestService,
                                       ControllerHelperService controllerHelperService,
                                       AppProcessingBreadcrumbService breadcrumbService) {
    this.consultationRequestService = consultationRequestService;
    this.controllerHelperService = controllerHelperService;
    this.breadcrumbService = breadcrumbService;
  }

  @GetMapping
  public ModelAndView renderRequestConsultation(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         @ModelAttribute("form") ConsultationRequestForm form) {
    return getRequestConsultationModelAndView(processingContext);
  }

  @PostMapping
  public ModelAndView postRequestConsultation(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") ConsultationRequestForm form,
                                              BindingResult bindingResult) {

    bindingResult = consultationRequestService.validate(form, bindingResult, processingContext.getPwaApplication());
    var appDetail = processingContext.getApplicationDetail();
    consultationRequestService.rebindFormCheckboxes(form);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getRequestConsultationModelAndView(processingContext), () -> {
          consultationRequestService.saveEntitiesAndStartWorkflow(form, appDetail, authenticatedUserAccount);
          return ReverseRouter.redirect(on(ConsultationController.class).renderConsultations(
              appDetail.getMasterPwaApplicationId(), appDetail.getPwaApplicationType(), null, null));
        });

  }

  private ModelAndView getRequestConsultationModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplicationDetail = processingContext.getApplicationDetail();

    var modelAndView = new ModelAndView("consultation/consultationRequest")
        .addObject("errorList", List.of())
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("consulteeGroups", consultationRequestService.getAllConsulteeGroups().stream()
          .sorted(Comparator.comparing(ConsulteeGroupDetail::getDisplayOrder,  Comparator.nullsLast(Comparator.naturalOrder()))
              .thenComparing(ConsulteeGroupDetail::getName))
          .collect(Collectors.toList()))
        .addObject("cancelUrl", ReverseRouter.route(on(ConsultationController.class).renderConsultations(
            pwaApplicationDetail.getMasterPwaApplicationId(), pwaApplicationDetail.getPwaApplicationType(), null, null)));

    breadcrumbService.fromConsultations(pwaApplicationDetail.getPwaApplication(), modelAndView, "Request consultations");

    return modelAndView;

  }

}