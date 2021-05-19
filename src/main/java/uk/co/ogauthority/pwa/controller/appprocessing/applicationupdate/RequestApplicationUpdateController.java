package uk.co.ogauthority.pwa.controller.appprocessing.applicationupdate;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.function.Function;
import javax.validation.Valid;
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
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.form.appprocessing.applicationupdate.ApplicationUpdateRequestForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.appprocessing.appupdaterequest.ApplicationUpdateRequestValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/request-application-update")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE})
public class RequestApplicationUpdateController {

  private final ControllerHelperService controllerHelperService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ApplicationUpdateRequestValidator applicationUpdateRequestValidator;
  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;

  @Autowired
  public RequestApplicationUpdateController(ControllerHelperService controllerHelperService,
                                            ApplicationUpdateRequestService applicationUpdateRequestService,
                                            ApplicationUpdateRequestValidator applicationUpdateRequestValidator,
                                            AppProcessingBreadcrumbService appProcessingBreadcrumbService) {
    this.controllerHelperService = controllerHelperService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.applicationUpdateRequestValidator = applicationUpdateRequestValidator;
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
  }


  @GetMapping
  public ModelAndView renderRequestUpdate(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          @ModelAttribute("form") ApplicationUpdateRequestForm form) {

    return whenZeroOpenUpdateRequests(processingContext, this::getModelAndView);
  }


  @PostMapping
  public ModelAndView requestUpdate(@PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    PwaAppProcessingContext processingContext,
                                    AuthenticatedUserAccount authenticatedUserAccount,
                                    @Valid @ModelAttribute("form") ApplicationUpdateRequestForm form,
                                    BindingResult bindingResult) {
    return whenZeroOpenUpdateRequests(processingContext, pwaApplicationDetail -> {
          var modelAndView = getModelAndView(processingContext);
          applicationUpdateRequestValidator.validate(form, bindingResult);
          return controllerHelperService.checkErrorsAndRedirect(
              bindingResult,
              modelAndView,
              () -> {
                applicationUpdateRequestService.submitApplicationUpdateRequest(
                    processingContext.getApplicationDetail(),
                    authenticatedUserAccount,
                    form
                );
                return ReverseRouter.redirect(on(CaseManagementController.class)
                    .renderCaseManagement(applicationId, pwaApplicationType, AppProcessingTab.TASKS, null, null));
              }
          );
        }
    );


  }

  private ModelAndView getModelAndView(PwaAppProcessingContext processingContext) {

    var pwaApplicationDetail = processingContext.getApplicationDetail();

    var caseManagementUrl = ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(
            pwaApplicationDetail.getMasterPwaApplicationId(),
            pwaApplicationDetail.getPwaApplicationType(),
            AppProcessingTab.TASKS,
            null,
            null
        ));

    var modelAndView = new ModelAndView("appprocessing/requestApplicationUpdate")
        .addObject("appRef", pwaApplicationDetail.getPwaApplicationRef())
        .addObject("errorList", List.of())
        .addObject("cancelUrl", caseManagementUrl)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplicationDetail.getPwaApplication(), modelAndView, "Request update");

    return modelAndView;

  }

  private ModelAndView whenZeroOpenUpdateRequests(PwaAppProcessingContext processingContext,
                                                  Function<PwaAppProcessingContext, ModelAndView> doWhenZeroUpdateRequests) {

    var pwaApplicationDetail = processingContext.getApplicationDetail();

    if (applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail)) {
      throw new AccessDeniedException(
          String.format("Pad_id: %s has open update request", pwaApplicationDetail.getId()));
    }

    return doWhenZeroUpdateRequests.apply(processingContext);

  }


}
