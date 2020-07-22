package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipeline;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ModifyPipelineService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.pipelines.ModifyPipelineValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/consented")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class ModifyPipelineController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ModifyPipelineService modifyPipelineService;
  private final ModifyPipelineValidator modifyPipelineValidator;
  private final ControllerHelperService controllerHelperService;

  public ModifyPipelineController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      ModifyPipelineService modifyPipelineService,
      ModifyPipelineValidator modifyPipelineValidator,
      ControllerHelperService controllerHelperService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.modifyPipelineService = modifyPipelineService;
    this.modifyPipelineValidator = modifyPipelineValidator;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView createImportConsentedPipelineModelAndView(PwaApplicationDetail detail) {
    var selectablePipelines = modifyPipelineService.getSelectableConsentedPipelines(detail);
    var pipelineSelection = selectablePipelines.stream()
        .collect(StreamUtils.toLinkedHashMap(named -> String.valueOf(named.getPipelineId()),
            NamedPipeline::getPipelineName));
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/importConsented")
        .addObject("consentedPipelines", pipelineSelection);
    applicationBreadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView,
        "Modify consented pipeline");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderImportConsentedPipeline(@PathVariable("applicationId") Integer applicationId,
                                                    @PathVariable("applicationType")
                                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                    PwaApplicationContext applicationContext,
                                                    @ModelAttribute("form") ModifyPipelineForm form) {
    return createImportConsentedPipelineModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postImportConsentedPipeline(@PathVariable("applicationId") Integer applicationId,
                                                  @PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  PwaApplicationContext applicationContext,
                                                  @ModelAttribute("form") ModifyPipelineForm form,
                                                  BindingResult bindingResult) {
    var detail = applicationContext.getApplicationDetail();
    modifyPipelineValidator.validate(form, bindingResult, detail);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        createImportConsentedPipelineModelAndView(detail),
        () -> {
          modifyPipelineService.importPipeline(detail, form);
          return ReverseRouter.redirect(on(PipelinesController.class)
              .renderPipelinesOverview(applicationId, pwaApplicationType, null));
        });
  }
}
