package uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/review")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.HUOO_VARIATION
})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
public class ReviewAndSubmitController {

  private static final String PAGE_NAME = "Review and submit";

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationSubmissionService pwaApplicationSubmissionService;
  private final ApplicationSummaryService applicationSummaryService;
  private final TemplateRenderingService templateRenderer;

  @Autowired
  public ReviewAndSubmitController(PwaApplicationRedirectService pwaApplicationRedirectService,
                                   PwaApplicationSubmissionService pwaApplicationSubmissionService,
                                   ApplicationSummaryService applicationSummaryService,
                                   TemplateRenderingService templateRenderer) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationSubmissionService = pwaApplicationSubmissionService;
    this.applicationSummaryService = applicationSummaryService;
    this.templateRenderer = templateRenderer;
  }


  @GetMapping
  public ModelAndView review(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") int applicationId,
      PwaApplicationContext applicationContext) {

    var modelAndView = new ModelAndView("pwaApplication/shared/submission/reviewAndSubmit");

    var summarisedSections = applicationSummaryService.summarise(applicationContext.getApplicationDetail());
    String combinedRenderedSummaryHtml = summarisedSections.stream()
        .map(summary -> templateRenderer.render(summary.getTemplatePath(), summary.getTemplateModel(), true))
        .collect(Collectors.joining());

    List<SidebarSectionLink> sidebarSectionLinks = summarisedSections.stream()
        .flatMap(o -> o.getSidebarSectionLinks().stream())
        .collect(Collectors.toList());

    modelAndView.addObject("combinedSummaryHtml", combinedRenderedSummaryHtml)
        .addObject("sidebarSectionLinks", sidebarSectionLinks)
        .addObject("taskListUrl", pwaApplicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()))
        .addObject("applicationReference", applicationContext.getPwaApplication().getAppReference());

    return modelAndView;

  }


  @PostMapping
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.SUBMIT)
  public ModelAndView submit(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") int applicationId,
      PwaApplicationContext applicationContext) {

    pwaApplicationSubmissionService.submitApplication(applicationContext.getUser(),
        applicationContext.getApplicationDetail());
    return ReverseRouter.redirect(
        on(SubmitConfirmationController.class).confirmation(applicationType, applicationId, null));

  }
}
