package uk.co.ogauthority.pwa.controller.consultations.responses;

import java.util.Objects;
import java.util.function.Supplier;
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
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.AssignResponderService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/consultation/{consultationRequestId}/assign-responder")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.ASSIGN_RESPONDER})
public class AssignResponderController {

  private final AssignResponderService assignResponderService;
  private final ControllerHelperService controllerHelperService;
  private final AppProcessingBreadcrumbService breadcrumbService;

  @Autowired
  public AssignResponderController(AssignResponderService assignResponderService,
                                   ControllerHelperService controllerHelperService,
                                   AppProcessingBreadcrumbService breadcrumbService) {
    this.assignResponderService = assignResponderService;
    this.controllerHelperService = controllerHelperService;
    this.breadcrumbService = breadcrumbService;
  }

  @GetMapping
  public ModelAndView renderAssignResponder(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("consultationRequestId") Integer consultationRequestId,
                                            PwaAppProcessingContext processingContext,
                                            @ModelAttribute("form") AssignResponderForm form) {

    return withAccessibleConsultation(processingContext, consultationRequestId, () ->
        getAssignResponderModelAndView(processingContext));

  }

  @PostMapping
  public ModelAndView postAssignResponder(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          @PathVariable("consultationRequestId") Integer consultationRequestId,
                                          PwaAppProcessingContext processingContext,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          @ModelAttribute("form") AssignResponderForm form,
                                          BindingResult bindingResult,
                                          RedirectAttributes redirectAttributes) {

    return withAccessibleConsultation(processingContext, consultationRequestId, () -> {

      var consultationRequest = processingContext.getActiveConsultationRequest().getConsultationRequest();

      var validatedBindingResult = assignResponderService.validate(form, bindingResult, consultationRequest);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getAssignResponderModelAndView(processingContext), () -> {
            assignResponderService.assignResponder(form, consultationRequest, authenticatedUserAccount);
            FlashUtils.success(redirectAttributes, "Responder assigned.");
            return CaseManagementUtils.redirectCaseManagement(processingContext);
          });

    });

  }

  private ModelAndView withAccessibleConsultation(PwaAppProcessingContext processingContext,
                                                  Integer consultationRequestId,
                                                  Supplier<ModelAndView> successSupplier) {

    // if consultation request linked to user on context is equal to the one we are hitting in the URL, ok to continue
    if (Objects.equals(processingContext.getConsultationRequestId(), consultationRequestId)) {
      return successSupplier.get();
    }

    // otherwise error
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot access the Assign responder page as they either do not have the " +
                "ASSIGN_RESPONDER permission and/or they are not a member of the consultee group of the consultation request.",
            processingContext.getUser().getWuaId()));


  }

  private ModelAndView getAssignResponderModelAndView(PwaAppProcessingContext processingContext) {

    var request = processingContext.getActiveConsultationRequest().getConsultationRequest();
    var responders = assignResponderService.getAllRespondersForRequest(request).stream()
        .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()), Person::getFullName));

    var modelAndView = new ModelAndView("consultation/responses/assignResponder")
        .addObject("responders", responders)
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext.getPwaApplication()))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("consulteeGroupName", processingContext.getActiveConsultationRequest().getConsulteeGroupName());

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Assign responder");

    return modelAndView;

  }


}