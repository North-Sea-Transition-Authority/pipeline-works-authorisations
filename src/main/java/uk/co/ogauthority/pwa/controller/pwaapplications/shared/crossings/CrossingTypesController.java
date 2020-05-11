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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingTypesForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings.CrossingTypesFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/types")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class CrossingTypesController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final CrossingTypesFormValidator crossingTypesFormValidator;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public CrossingTypesController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      CrossingTypesFormValidator crossingTypesFormValidator,
      PwaApplicationDetailService pwaApplicationDetailService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.crossingTypesFormValidator = crossingTypesFormValidator;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  private ModelAndView createModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/crossingTypes");
    applicationBreadcrumbService.fromCrossings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Crossing types");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderForm(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingTypesForm form,
      PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    form.setCablesCrossed(detail.getCablesCrossed());
    form.setPipelinesCrossed(detail.getPipelinesCrossed());
    form.setMedianLineCrossed(detail.getMedianLineCrossed());
    return createModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postForm(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                               @PathVariable("applicationId") Integer applicationId,
                               @Valid @ModelAttribute("form") CrossingTypesForm form,
                               BindingResult bindingResult,
                               PwaApplicationContext applicationContext,
                               ValidationType validationType) {
    var detail = applicationContext.getApplicationDetail();
    if (validationType.equals(ValidationType.FULL)) {
      crossingTypesFormValidator.validate(form, bindingResult);
    }
    return ControllerUtils.checkErrorsAndRedirect(bindingResult, createModelAndView(detail), () -> {
      pwaApplicationDetailService.updateCrossingStatus(detail, form);
      return ReverseRouter.redirect(on(CrossingAgreementsController.class).renderCrossingAgreementsOverview(
          detail.getPwaApplicationType(), applicationId, null, null));
    });
  }

}
