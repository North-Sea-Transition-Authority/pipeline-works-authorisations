package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.ConsentedPipelineService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/consented")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class ConsentedPipelineController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ConsentedPipelineService consentedPipelineService;

  public ConsentedPipelineController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      ConsentedPipelineService consentedPipelineService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.consentedPipelineService = consentedPipelineService;
  }

  private ModelAndView createImportConsentedPipelineModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/importConsented")
        .addObject("consentedPipelines", consentedPipelineService.getSelectableConsentedPipelines(detail));
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
    var detail = applicationContext.getApplicationDetail();
    return createImportConsentedPipelineModelAndView(detail);
  }
}
