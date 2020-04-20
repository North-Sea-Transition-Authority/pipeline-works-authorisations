package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import javax.validation.Valid;
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
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddCableCrossingForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/cable")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class CableCrossingController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadCableCrossingService padCableCrossingService;

  public CableCrossingController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadCableCrossingService padCableCrossingService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padCableCrossingService = padCableCrossingService;
  }

  private ModelAndView createRenderModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/cableCrossing");
    applicationBreadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView, "Add cable crossing");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderAddCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddCableCrossingForm form,
      PwaApplicationContext applicationContext) {

    return createRenderModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postAddCableCrossings(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @Valid @ModelAttribute("form") AddCableCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, createRenderModelAndView(detail), () -> {
      padCableCrossingService.createCableCrossing(detail, form);
      return ReverseRouter.redirect(on(CrossingAgreementsController.class)
              .renderCrossingAgreementsOverview(applicationType, null, null),
          Map.of("applicationId", detail.getMasterPwaApplicationId()));
    });
  }

  @GetMapping("/{crossingId}/edit")
  public ModelAndView renderEditCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      @ModelAttribute("form") AddCableCrossingForm form,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    var modelAndView = createRenderModelAndView(detail);
    var cableCrossing = padCableCrossingService.getCableCrossing(detail, crossingId);
    padCableCrossingService.mapCrossingToForm(cableCrossing, form);
    return modelAndView;
  }

  @PostMapping("/{crossingId}/edit")
  public ModelAndView postEditCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      @Valid @ModelAttribute("form") AddCableCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, createRenderModelAndView(detail), () -> {
      padCableCrossingService.updateCableCrossing(detail, crossingId, form);
      return ReverseRouter.redirect(on(CrossingAgreementsController.class)
              .renderCrossingAgreementsOverview(applicationType, null, null),
          Map.of("applicationId", detail.getMasterPwaApplicationId()));
    });
  }

  @PostMapping("/{crossingId}/delete")
  public ModelAndView postRemoveCableCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("crossingId") Integer crossingId,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    padCableCrossingService.removeCableCrossing(detail, crossingId);
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(applicationType, null, null),
        Map.of("applicationId", detail.getMasterPwaApplicationId()));
  }

}
