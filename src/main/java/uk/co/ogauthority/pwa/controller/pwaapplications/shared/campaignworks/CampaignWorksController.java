package uk.co.ogauthority.pwa.controller.pwaapplications.shared.campaignworks;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/campaign-works")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class CampaignWorksController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPipelineService padPipelineService;
  private final CampaignWorksService campaignWorksService;

  @Autowired
  public CampaignWorksController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PadPipelineService padPipelineService,
      CampaignWorksService campaignWorksService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPipelineService = padPipelineService;
    this.campaignWorksService = campaignWorksService;
  }


  private ModelAndView createAddWorkScheduleModelAndView(PwaApplicationContext applicationContext,
                                                         ScreenActionType screenActionType) {
    var modelAndView = new ModelAndView("pwaApplication/shared/campaignworks/workScheduleForm")
        .addObject("cancelUrl", ReverseRouter.route(on(CampaignWorksController.class)
            .renderSummary(applicationContext.getApplicationType(), applicationContext.getMasterPwaApplicationId(),
                null)))
        .addObject("pipelineViews", padPipelineService.getPipelineOverviews(applicationContext.getApplicationDetail()))
        .addObject("screenActionType", screenActionType);

    applicationBreadcrumbService.fromCampaignWorksOverview(applicationContext.getPwaApplication(), modelAndView,
        "Add work schedule");

    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderSummary(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") int applicationId,
                                    PwaApplicationContext applicationContext) {
    var modelAndView = new ModelAndView("pwaApplication/shared/campaignworks/campaignWorks")
        .addObject("dependencySectionName", ApplicationTask.PROJECT_INFORMATION.getDisplayName())
        .addObject("dependencySectionUrl", ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(pwaApplicationType, applicationId, null, null)))
        .addObject("urlFactory", new CampaignWorksUrlFactory(applicationContext.getApplicationDetail()))
        .addObject("workScheduleViewList", campaignWorksService.getWorkScheduleViews(applicationContext.getApplicationDetail()));
    applicationBreadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView, "Campaign Works");
    return modelAndView;
  }

  @GetMapping("/add")
  public ModelAndView renderAddWorkSchedule(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") int applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") WorkScheduleForm form) {

    return createAddWorkScheduleModelAndView(applicationContext, ScreenActionType.ADD);
  }

  @PostMapping("/add")
  public ModelAndView addWorkSchedule(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") int applicationId,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") WorkScheduleForm form,
                                      BindingResult bindingResult) {

    campaignWorksService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        createAddWorkScheduleModelAndView(applicationContext, ScreenActionType.ADD), () -> {
          campaignWorksService.addCampaignWorkScheduleFromForm(form, applicationContext.getApplicationDetail());
          return ReverseRouter.redirect(
              on(CampaignWorksController.class).renderSummary(pwaApplicationType, applicationId, null));

        });
  }


}
