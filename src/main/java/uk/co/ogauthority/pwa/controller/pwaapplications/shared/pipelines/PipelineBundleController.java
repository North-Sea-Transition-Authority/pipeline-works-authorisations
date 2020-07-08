package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadBundleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadBundleSummaryView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pipelines.AddBundleValidator;
import uk.co.ogauthority.pwa.validators.pipelines.EditBundleValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/bundle")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
public class PipelineBundleController {

  private final PadPipelineService padPipelineService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PadBundleService padBundleService;
  private final AddBundleValidator addBundleValidator;
  private final EditBundleValidator editBundleValidator;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PipelineBundleController(
      PadPipelineService padPipelineService,
      ApplicationBreadcrumbService breadcrumbService,
      PadBundleService padBundleService,
      AddBundleValidator addBundleValidator,
      EditBundleValidator editBundleValidator,
      ControllerHelperService controllerHelperService) {
    this.padPipelineService = padPipelineService;
    this.breadcrumbService = breadcrumbService;
    this.padBundleService = padBundleService;
    this.addBundleValidator = addBundleValidator;
    this.editBundleValidator = editBundleValidator;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getBundleModelAndView(PwaApplicationContext context, ScreenActionType type) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/bundle")
        .addObject("screenActionType", type)
        .addObject("backUrl", ReverseRouter.route(on(PipelinesController.class)
            .renderPipelinesOverview(context.getMasterPwaApplicationId(), context.getApplicationType(), null)))
        .addObject("pipelineOverviews",
            padPipelineService.getApplicationPipelineOverviews(context.getApplicationDetail()));
    breadcrumbService.fromPipelinesOverview(context.getPwaApplication(), modelAndView,
        type.getActionText() + " bundle");
    return modelAndView;
  }

  private ModelAndView getRemoveBundleModelAndView(PwaApplicationContext context, PadBundleSummaryView summaryView) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/removeBundle")
        .addObject("backUrl", ReverseRouter.route(on(PipelinesController.class)
            .renderPipelinesOverview(context.getMasterPwaApplicationId(), context.getApplicationType(), null)))
        .addObject("summaryView", summaryView);
    breadcrumbService.fromPipelinesOverview(context.getPwaApplication(), modelAndView, "Remove bundle");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderAddBundle(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") BundleForm bundleForm) {
    return getBundleModelAndView(applicationContext, ScreenActionType.ADD);
  }

  @PostMapping
  public ModelAndView postAddBundle(@PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    PwaApplicationContext applicationContext,
                                    @ModelAttribute("form") BundleForm bundleForm,
                                    BindingResult bindingResult) {

    addBundleValidator.validate(bundleForm, bindingResult, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getBundleModelAndView(applicationContext, ScreenActionType.ADD), () -> {
          padBundleService.createBundleAndLinks(applicationContext.getApplicationDetail(), bundleForm);
          return ReverseRouter.redirect(on(PipelinesController.class)
              .renderPipelinesOverview(applicationId, pwaApplicationType, null));
        });
  }

  @GetMapping("/{bundleId}/edit")
  public ModelAndView renderEditBundle(@PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("bundleId") Integer bundleId,
                                       PwaApplicationContext applicationContext,
                                       @ModelAttribute("form") BundleForm bundleForm) {

    var bundleView = padBundleService.getBundleView(applicationContext.getApplicationDetail(), bundleId);
    padBundleService.mapBundleViewToForm(bundleView, bundleForm);

    return getBundleModelAndView(applicationContext, ScreenActionType.EDIT);
  }

  @PostMapping("/{bundleId}/edit")
  public ModelAndView postEditBundle(@PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("bundleId") Integer bundleId,
                                     PwaApplicationContext applicationContext,
                                     @ModelAttribute("form") BundleForm bundleForm,
                                     BindingResult bindingResult) {
    var bundle = padBundleService.getBundle(applicationContext.getApplicationDetail(), bundleId);
    editBundleValidator.validate(bundleForm, bindingResult, applicationContext.getApplicationDetail(), bundle);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getBundleModelAndView(applicationContext, ScreenActionType.EDIT), () -> {
          padBundleService.updateBundleAndLinks(bundle, bundleForm);
          return ReverseRouter.redirect(on(PipelinesController.class)
              .renderPipelinesOverview(applicationId, pwaApplicationType, null));
        });
  }

  @GetMapping("/{bundleId}/remove")
  public ModelAndView renderRemoveBundle(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("bundleId") Integer bundleId,
                                         PwaApplicationContext applicationContext) {
    var summaryView = padBundleService.getBundleSummaryView(applicationContext.getApplicationDetail(), bundleId);
    return getRemoveBundleModelAndView(applicationContext, summaryView);
  }

  @PostMapping("/{bundleId}/remove")
  public ModelAndView postRemoveBundle(@PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("bundleId") Integer bundleId,
                                       PwaApplicationContext applicationContext,
                                       RedirectAttributes redirectAttributes) {

    var bundle = padBundleService.getBundle(applicationContext.getApplicationDetail(), bundleId);
    padBundleService.removeBundle(bundle);
    FlashUtils.info(redirectAttributes,
        String.format("Removed bundle (%s) from application", bundle.getBundleName()));
    return ReverseRouter.redirect(on(PipelinesController.class)
        .renderPipelinesOverview(applicationId, pwaApplicationType, null));
  }

}
