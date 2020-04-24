package uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.generic.SummaryForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiralityChartFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiralityChartUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.TechnicalDrawingsService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
public class TechnicalDrawingsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final AdmiralityChartFileService admiralityChartFileService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final TechnicalDrawingsService technicalDrawingsService;

  @Autowired
  public TechnicalDrawingsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      AdmiralityChartFileService admiralityChartFileService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      TechnicalDrawingsService technicalDrawingsService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.admiralityChartFileService = admiralityChartFileService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.technicalDrawingsService = technicalDrawingsService;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/overview")
        .addObject("admiralityChartFileViews",
            admiralityChartFileService.getAdmiralityChartFileViews(detail, ApplicationFileLinkStatus.FULL))
        .addObject("admiralityOptional", !admiralityChartFileService.isUploadRequired(detail))
        .addObject("admiralityChartUrlFactory", new AdmiralityChartUrlFactory(detail))
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(detail.getPwaApplication()));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView, "Technical drawings");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderOverview(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("applicationId") Integer applicationId,
                                     PwaApplicationContext applicationContext,
                                     AuthenticatedUserAccount user) {
    return getOverviewModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postOverview(@PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   @PathVariable("applicationId") Integer applicationId,
                                   @Valid SummaryForm form,
                                   BindingResult bindingResult,
                                   PwaApplicationContext applicationContext,
                                   AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    bindingResult = technicalDrawingsService.validate(form, bindingResult, ValidationType.FULL, detail);
    if (bindingResult.hasErrors()) {
      return getOverviewModelAndView(detail)
          .addObject("errorMessage", "The admirality chart section is invalid");
    }
    return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
  }

}
