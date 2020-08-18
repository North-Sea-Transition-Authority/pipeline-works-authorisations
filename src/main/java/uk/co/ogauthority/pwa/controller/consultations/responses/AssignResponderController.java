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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/consultation/{consultationRequestId}/assign-responder")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.ASSIGN_RESPONDER})
public class AssignResponderController {

  private final AssignResponderService assignResponderService;
  private final ConsultationRequestService consultationRequestService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public AssignResponderController(
      AssignResponderService assignResponderService,
      ConsultationRequestService consultationRequestService,
      ControllerHelperService controllerHelperService) {
    this.assignResponderService = assignResponderService;
    this.consultationRequestService = consultationRequestService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderAssignResponder(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("consultationRequestId") Integer consultationRequestId,
                                            PwaAppProcessingContext processingContext,
                                            AuthenticatedUserAccount authenticatedUserAccount,
                                            @ModelAttribute("form") AssignResponderForm form,
                                            BindingResult bindingResult) {
    var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);
    if (assignResponderService.isUserMemberOfRequestGroup(processingContext.getUser(), consultationRequest)) {
      return getAssignResponderModelAndView(consultationRequest, authenticatedUserAccount);
    }
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot access the Assign responder page as they either do not have the " +
            "ASSIGN_RESPONDER permission and/or they are not a member of the consultee group of the consultation request.",
            processingContext.getUser().getWuaId()));
  }


  @GetMapping("/re-assign")
  public ModelAndView renderReAssignResponder(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("consultationRequestId") Integer consultationRequestId,
                                            PwaAppProcessingContext processingContext,
                                            AuthenticatedUserAccount authenticatedUserAccount,
                                            @ModelAttribute("form") AssignResponderForm form,
                                            BindingResult bindingResult) {
    var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);
    if (assignResponderService.isUserMemberOfRequestGroup(processingContext.getUser(), consultationRequest)) {
      return getAssignResponderModelAndView(consultationRequest, authenticatedUserAccount);
    }
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot access the re-assign responder page as they either do not have the " +
                "ASSIGN_RESPONDER permission and/or they are not a member of the consultee group of the consultation request.",
            processingContext.getUser().getWuaId()));
  }


  @PostMapping
  public ModelAndView postAssignResponder(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          @PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("consultationRequestId") Integer consultationRequestId,
                                          PwaAppProcessingContext processingContext,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          @ModelAttribute("form") AssignResponderForm form,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes) {

    var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);
    bindingResult = assignResponderService.validate(form, bindingResult, consultationRequest);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAssignResponderModelAndView(consultationRequest, authenticatedUserAccount), () -> {
          assignResponderService.assignUserAndCompleteWorkflow(form, consultationRequest, authenticatedUserAccount);
          FlashUtils.success(
              redirectAttributes, "Responder assigned.");
          return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, authenticatedUserAccount, null));
        });
  }


  @PostMapping("/re-assign")
  public ModelAndView postReAssignResponder(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          @PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("consultationRequestId") Integer consultationRequestId,
                                          PwaAppProcessingContext processingContext,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          @ModelAttribute("form") AssignResponderForm form,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes) {

    var consultationRequest = consultationRequestService.getConsultationRequestById(consultationRequestId);
    bindingResult = assignResponderService.validate(form, bindingResult, consultationRequest);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAssignResponderModelAndView(consultationRequest, authenticatedUserAccount), () -> {
          assignResponderService.reassignUser(form, consultationRequest, authenticatedUserAccount);
          FlashUtils.success(
              redirectAttributes, "Responder re-assigned.");
          return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, authenticatedUserAccount, null));
        });
  }




  private ModelAndView getAssignResponderModelAndView(ConsultationRequest consultationRequest,
                                                      AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView("consultation/responses/assignResponder")
        .addObject("responders", assignResponderService.getAllRespondersForRequest(consultationRequest).stream().collect(
            StreamUtils.toLinkedHashMap(
                person -> String.valueOf(person.getId().asInt()), Person::getFullName)))
        .addObject("cancelUrl",
                ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, authenticatedUserAccount, null)));
  }


}