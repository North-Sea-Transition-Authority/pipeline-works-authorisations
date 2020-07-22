package uk.co.ogauthority.pwa.controller.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/request-consultation")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW_CONSULTATIONS})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class ConsultationRequestController {

  private final ConsultationRequestService consultationRequestService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public ConsultationRequestController(
      ConsultationRequestService consultationRequestService,
      ControllerHelperService controllerHelperService) {
    this.consultationRequestService = consultationRequestService;
    this.controllerHelperService = controllerHelperService;
  }






  @GetMapping
  public ModelAndView renderRequestConsultation(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount,
                                         @ModelAttribute("form") ConsultationRequestForm form) {
    return getRequestConsultationModelAndView(processingContext.getApplicationDetail(), authenticatedUserAccount);
  }


  @PostMapping
  public ModelAndView postAddFluidCompositionInfo(@PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  @PathVariable("applicationId") Integer applicationId,
                                                  PwaAppProcessingContext processingContext,
                                                  AuthenticatedUserAccount authenticatedUserAccount,
                                                  @ModelAttribute("form") ConsultationRequestForm form,
                                                  BindingResult bindingResult,
                                                  ValidationType validationType) {

    bindingResult = consultationRequestService.validate(form, bindingResult, validationType, processingContext.getApplicationDetail());
    var appDetail = processingContext.getApplicationDetail();

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getRequestConsultationModelAndView(processingContext.getApplicationDetail(), authenticatedUserAccount), () -> {
          //process
//          return ReverseRouter.route(on(ConsultationController.class).renderConsultation(
//              appDetail.getMasterPwaApplicationId(), appDetail.getPwaApplicationType(), null, null));
          return getRequestConsultationModelAndView(appDetail, authenticatedUserAccount);
        });

  }


  private ModelAndView getRequestConsultationModelAndView(
      PwaApplicationDetail pwaApplicationDetail, AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView("consultation/consultationRequest")
        .addObject("appRef", pwaApplicationDetail.getPwaApplicationRef())
        .addObject("consulteeGroups", consultationRequestService.getConsulteeGroups(authenticatedUserAccount));
  }


}