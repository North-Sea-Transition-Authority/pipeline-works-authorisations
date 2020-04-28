package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import org.springframework.beans.factory.annotation.Autowired;
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
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/pipeline")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PipelineCrossingController {

  private final PadPipelineCrossingService padPipelineCrossingService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public PipelineCrossingController(
      PadPipelineCrossingService padPipelineCrossingService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.padPipelineCrossingService = padPipelineCrossingService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getCrossingModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("");
    applicationBreadcrumbService.fromCrossings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Pipeline crossings");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderAddCrossing(@PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("applicationId") Integer applicationId,
                                        @ModelAttribute("form") PipelineCrossingForm form,
                                        PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    return getCrossingModelAndView(detail);
  }

}
