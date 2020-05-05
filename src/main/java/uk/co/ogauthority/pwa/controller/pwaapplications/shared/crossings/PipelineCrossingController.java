package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.rest.PortalOrganisationUnitRestController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingOwnerService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings.PipelineCrossingFormValidator;

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
  private final PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;
  private final PipelineCrossingFormValidator pipelineCrossingFormValidator;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public PipelineCrossingController(
      PadPipelineCrossingService padPipelineCrossingService,
      PadPipelineCrossingOwnerService padPipelineCrossingOwnerService,
      PipelineCrossingFormValidator pipelineCrossingFormValidator,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.padPipelineCrossingService = padPipelineCrossingService;
    this.padPipelineCrossingOwnerService = padPipelineCrossingOwnerService;
    this.pipelineCrossingFormValidator = pipelineCrossingFormValidator;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getCrossingModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                               ScreenActionType screenActionType) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/pipeline/addPipelineCrossing")
        .addObject("screenActionType", screenActionType)
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)))
        .addObject("orgsRestUrl", SearchSelectorService.route(on(PortalOrganisationUnitRestController.class)
            .searchPortalOrgUnits(null)));
    applicationBreadcrumbService.fromCrossings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        screenActionType.getActionText() + " pipeline crossing");
    return modelAndView;
  }

  private ModelAndView repopulateOnError(PwaApplicationDetail pwaApplicationDetail, ScreenActionType screenActionType,
                                         PipelineCrossingForm pipelineCrossingForm) {
    var owners = padPipelineCrossingService.getPrepopulatedSearchSelectorItems(
        pipelineCrossingForm.getPipelineOwners());
    return getCrossingModelAndView(pwaApplicationDetail, screenActionType)
        .addObject("preselectedOwners", owners);

  }

  @GetMapping
  public ModelAndView renderAddCrossing(@PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("applicationId") Integer applicationId,
                                        @ModelAttribute("form") PipelineCrossingForm form,
                                        PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    return getCrossingModelAndView(detail, ScreenActionType.ADD);
  }

  @PostMapping
  public ModelAndView postAddCrossings(@PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       @Valid @ModelAttribute("form") PipelineCrossingForm form,
                                       BindingResult bindingResult,
                                       PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    pipelineCrossingFormValidator.validate(form, bindingResult);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, repopulateOnError(detail, ScreenActionType.ADD, form),
        () -> {
          padPipelineCrossingService.createPipelineCrossings(detail, form);
          return ReverseRouter.redirect(on(CrossingAgreementsController.class)
              .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
                  null, null));
        });
  }

  @GetMapping("/{crossingId}/edit")
  public ModelAndView renderEditCrossing(@PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("crossingId") Integer crossingId,
                                         @ModelAttribute("form") PipelineCrossingForm form,
                                         PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    form.setPipelineCrossed(crossing.getPipelineCrossed());
    form.setPipelineFullyOwnedByOrganisation(crossing.getPipelineFullyOwnedByOrganisation());
    return getCrossingModelAndView(detail, ScreenActionType.EDIT)
        .addObject("preselectedOwners", padPipelineCrossingOwnerService.getOwnerPrepopulationFormAttribute(crossing));
  }

  @PostMapping("/{crossingId}/edit")
  public ModelAndView postEditCrossing(@PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       @PathVariable("crossingId") Integer crossingId,
                                       @Valid @ModelAttribute("form") PipelineCrossingForm form,
                                       BindingResult bindingResult,
                                       PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    pipelineCrossingFormValidator.validate(form, bindingResult);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, repopulateOnError(detail, ScreenActionType.EDIT, form),
        () -> {
          padPipelineCrossingService.updatePipelineCrossing(crossing, form);
          return ReverseRouter.redirect(on(CrossingAgreementsController.class)
              .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
                  null, null));
        });
  }

  @GetMapping("/{crossingId}/remove")
  public ModelAndView renderRemoveCrossing(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("crossingId") Integer crossingId,
                                           PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/pipeline/removePipelineCrossing")
        .addObject("view", padPipelineCrossingService.getPipelineCrossingView(crossing))
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), applicationId, null, null)));
    applicationBreadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView, "Remove cable crossing");
    return modelAndView;
  }

  @PostMapping("/{crossingId}/remove")
  public ModelAndView postRemoveCrossing(@PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("crossingId") Integer crossingId,
                                         PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var crossing = padPipelineCrossingService.getPipelineCrossing(detail, crossingId);
    padPipelineCrossingService.deleteCascade(crossing);
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(),
            null, null));
  }
}
