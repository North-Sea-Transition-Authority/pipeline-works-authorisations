package uk.co.ogauthority.pwa.controller.consultations.responses;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationResponseValidator;

@Controller
@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/consultation/{consultationRequestId}/respond")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.CONSULTATION_RESPONDER})
public class ConsultationResponseController {

  private final ConsultationResponseService consultationResponseService;
  private final ConsultationViewService consultationViewService;
  private final ControllerHelperService controllerHelperService;
  private final AppProcessingBreadcrumbService breadcrumbService;
  private ConsultationResponseValidator consultationResponseValidator;

  @Autowired
  public ConsultationResponseController(ConsultationResponseService consultationResponseService,
                                        ConsultationViewService consultationViewService,
                                        ControllerHelperService controllerHelperService,
                                        AppProcessingBreadcrumbService breadcrumbService,
                                        ConsultationResponseValidator consultationResponseValidator) {
    this.consultationResponseService = consultationResponseService;
    this.consultationViewService = consultationViewService;
    this.controllerHelperService = controllerHelperService;
    this.breadcrumbService = breadcrumbService;
    this.consultationResponseValidator = consultationResponseValidator;
  }

  @GetMapping
  public ModelAndView renderResponder(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("consultationRequestId") Integer consultationRequestId,
                                      PwaAppProcessingContext processingContext,
                                      @ModelAttribute("form") ConsultationResponseForm form) {

    return withAccessibleConsultation(processingContext, consultationRequestId, () ->
      getResponderModelAndView(processingContext, form));

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

    return withAccessibleConsultation(processingContext, consultationRequestId, () -> {

      consultationResponseValidator.validate(form, bindingResult, processingContext.getActiveConsultationRequestOrThrow());

      var request = processingContext.getActiveConsultationRequestOrThrow().getConsultationRequest();

      return controllerHelperService.checkErrorsAndRedirect(bindingResult,
          getResponderModelAndView(processingContext, form), () -> {
            consultationResponseService.saveResponseAndCompleteWorkflow(form, request, authenticatedUserAccount);
            return CaseManagementUtils.redirectCaseManagement(processingContext);
          });

    });

  }

  private ModelAndView withAccessibleConsultation(PwaAppProcessingContext processingContext,
                                                  Integer consultationRequestId,
                                                  Supplier<ModelAndView> successSupplier) {

    // if consultation request linked to user on context is equal to the one we are hitting in the URL, ok to continue
    if (Objects.equals(processingContext.getActiveConsultationRequestId(), consultationRequestId)) {
      return successSupplier.get();
    }

    // otherwise error
    throw new AccessDeniedException(
        String.format("User with WUA ID: %s cannot respond to consultation request with id [%s] as they are not the assigned responder",
            processingContext.getUser().getWuaId(),
            consultationRequestId));


  }

  private ModelAndView getResponderModelAndView(PwaAppProcessingContext processingContext,
                                                ConsultationResponseForm consultationResponseForm) {

    var application = processingContext.getPwaApplication();
    var requestDto = processingContext.getActiveConsultationRequestOrThrow();

    var responseOptionGroupMap = requestDto.getConsultationResponseOptionGroups().stream()
        .sorted(Comparator.comparing(ConsultationResponseOptionGroup::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Function.identity(), ConsultationResponseOptionGroup::getOptions));

    // todo pwa-1359 update for EMT changes
    consultationResponseForm.setConsultationResponseOptionGroup(responseOptionGroupMap.entrySet().iterator().next().getKey());

    var modelAndView = new ModelAndView("consultation/responses/responderForm")
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(application))
        .addObject("responseOptionGroupMap", responseOptionGroupMap)
        .addObject("appRef", processingContext.getPwaApplication().getAppReference())
        .addObject("previousResponses", consultationViewService
            .getConsultationRequestViewsRespondedOnly(application, requestDto.getConsultationRequest()))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("consulteeGroupName", requestDto.getConsulteeGroupName());

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Consultation response");

    return modelAndView;

  }

}