package uk.co.ogauthority.pwa.controller.consultations.responses;

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
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/consultation/{consultationRequestId}/respond")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.CONSULTATION_RESPONDER})
public class ConsultationResponseController {

  private final ConsultationResponseService consultationResponseService;
  private final ConsultationRequestService consultationRequestService;
  private final ConsultationViewService consultationViewService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public ConsultationResponseController(ConsultationResponseService consultationResponseService,
                                        ConsultationRequestService consultationRequestService,
                                        ConsultationViewService consultationViewService,
                                        ControllerHelperService controllerHelperService) {
    this.consultationResponseService = consultationResponseService;
    this.consultationRequestService = consultationRequestService;
    this.consultationViewService = consultationViewService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderResponder(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("consultationRequestId") Integer consultationRequestId,
                                      PwaAppProcessingContext processingContext,
                                      AuthenticatedUserAccount authenticatedUserAccount,
                                      @ModelAttribute("form") ConsultationResponseForm form) {

    var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);

    if (consultationResponseService.isUserAssignedResponderForConsultation(authenticatedUserAccount, consultationRequest)) {
      return getResponderModelAndView(
          authenticatedUserAccount, processingContext.getPwaApplication().getAppReference(), processingContext.getPwaApplication(), consultationRequest);
    }

    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot access the Responder page as they either do not have the " +
            "CONSULTATION_RESPONDER permission and/or they are not the assigned responder for the consultation request with id %s",
            processingContext.getUser().getWuaId(),
            consultationRequestId));

  }

  @PostMapping
  public ModelAndView postResponder(@PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("consultationRequestId") Integer consultationRequestId,
                                    PwaAppProcessingContext processingContext,
                                    AuthenticatedUserAccount authenticatedUserAccount,
                                    @ModelAttribute("form") ConsultationResponseForm form,
                                    BindingResult bindingResult) {

    var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);

    if (!consultationResponseService.isUserAssignedResponderForConsultation(authenticatedUserAccount, consultationRequest)) {
      throw new AccessDeniedException(
          String.format("User with wua id [%s] can't respond to consultation request with id [%s] as they are not the assigned responder.",
          authenticatedUserAccount.getWuaId(),
          consultationRequestId));
    }

    bindingResult = consultationResponseService.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getResponderModelAndView(authenticatedUserAccount, processingContext.getPwaApplication().getAppReference(),
            processingContext.getPwaApplication(), consultationRequest), () -> {
          consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, authenticatedUserAccount);
          return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, authenticatedUserAccount, null));
        });

  }

  private ModelAndView getResponderModelAndView(AuthenticatedUserAccount authenticatedUserAccount,
                                                String appReference, PwaApplication pwaApplication, ConsultationRequest consultationRequest) {
    return new ModelAndView("consultation/responses/responderForm")
        .addObject("cancelUrl",
                ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, authenticatedUserAccount, null)))
        .addObject("responseOptions", ConsultationResponseOption.asList())
        .addObject("appRef", appReference)
        .addObject("previousResponses", consultationViewService.getConsultationRequestViewsRespondedOnly(pwaApplication, consultationRequest));
  }

}