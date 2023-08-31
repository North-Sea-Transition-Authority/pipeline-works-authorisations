package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.controller.PipelinesTaskListController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/claim")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class PadPipelineTransferClaimController {

  private final ControllerHelperService controllerHelperService;

  private final PadPipelineTransferService padPipelineTransferService;

  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  public PadPipelineTransferClaimController(ControllerHelperService controllerHelperService,
                                            PadPipelineTransferService padPipelineTransferService,
                                            ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.controllerHelperService = controllerHelperService;
    this.padPipelineTransferService = padPipelineTransferService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  @GetMapping
  public ModelAndView renderClaimPipelineForm(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaApplicationContext applicationContext,
                                              @ModelAttribute("form") PadPipelineTransferClaimForm form) {
    return getClaimPipelineModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView submitClaimPipelineForm(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaApplicationContext applicationContext,
                                              @ModelAttribute("form") PadPipelineTransferClaimForm form,
                                              BindingResult bindingResult) {
    var detail = applicationContext.getApplicationDetail();
    var validatedBindingResult = padPipelineTransferService.validateClaimForm(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(
        validatedBindingResult,
        getClaimPipelineModelAndView(detail),
        () -> {
          padPipelineTransferService.claimPipeline(form, detail);
          return ReverseRouter.redirect(on(PipelinesTaskListController.class)
              .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null));
        }
    );
  }

  private ModelAndView getClaimPipelineModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/transferClaimPipeline")
        .addObject("claimablePipelines", padPipelineTransferService.getClaimablePipelinesForForm(detail.getResourceType()))
        .addObject("backUrl", ReverseRouter.route(on(PipelinesTaskListController.class)
            .renderPipelinesOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null, null)));

    applicationBreadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView, "Transfer from another PWA");

    return modelAndView;
  }

}
