package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.EnumSet;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.InitialTaskListController;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.EnvDecomForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.validators.PadEnvDecomValidator;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/env-decom")
public class EnvironmentalDecomController {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private final PadEnvDecomValidator validator;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;

  private EnumSet<PwaApplicationType> allowedApplicationTypes = EnumSet.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING,
      PwaApplicationType.DEPOSIT_CONSENT
  );

  @Autowired
  public EnvironmentalDecomController(
      PwaApplicationDetailService pwaApplicationDetailService,
      PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService,
      PadEnvDecomValidator validator,
      ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padEnvironmentalDecommissioningService = padEnvironmentalDecommissioningService;
    this.validator = validator;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
  }

  private ModelAndView getAdminDetailsModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/environmentalAndDecommissioning")
        .addObject("hseSafetyZones", HseSafetyZone.stream()
            .sorted(Comparator.comparing(HseSafetyZone::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, HseSafetyZone::getDisplayText)))
        .addObject("environmentalConditions", EnvironmentalCondition.stream()
            .sorted(Comparator.comparing(EnvironmentalCondition::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, EnvironmentalCondition::getConditionText)))
        .addObject("decommissioningConditions", DecommissioningCondition.stream()
            .sorted(Comparator.comparing(DecommissioningCondition::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, DecommissioningCondition::getConditionText)));
    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView, "Environmental and decommissioning");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderAdminDetails(@PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("applicationId") Integer applicationId,
                                         @ModelAttribute("form") EnvDecomForm form,
                                         AuthenticatedUserAccount user) {
    ensureAllowed(pwaApplicationType);
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {
      var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
      var modelAndView = getAdminDetailsModelAndView(detail);
      padEnvironmentalDecommissioningService.mapEntityToForm(envDecomData, form);
      return modelAndView;
    });
  }

  @PostMapping(params = "Complete")
  public ModelAndView postCompleteAdminDetails(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               @Valid @ModelAttribute("form") EnvDecomForm form,
                                               BindingResult bindingResult,
                                               AuthenticatedUserAccount user) {
    ensureAllowed(pwaApplicationType);
    validator.validate(form, bindingResult);
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        ControllerUtils.validateAndRedirect(bindingResult, getAdminDetailsModelAndView(detail), () -> {
          var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
          padEnvironmentalDecommissioningService.saveEntityUsingForm(envDecomData, form);
          return ReverseRouter.redirect(on(InitialTaskListController.class).viewTaskList(applicationId));
        }));
  }

  @PostMapping(params = "Save and complete later")
  public ModelAndView postContinueAdminDetails(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               @ModelAttribute("form") EnvDecomForm form,
                                               BindingResult bindingResult,
                                               AuthenticatedUserAccount user) {
    ensureAllowed(pwaApplicationType);
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        ControllerUtils.validateAndRedirect(bindingResult, getAdminDetailsModelAndView(detail), () -> {
          var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
          padEnvironmentalDecommissioningService.saveEntityUsingForm(envDecomData, form);
          return ReverseRouter.redirect(on(InitialTaskListController.class).viewTaskList(applicationId));
        })
    );
  }

  private void ensureAllowed(PwaApplicationType pwaApplicationType) {
    var allowed = allowedApplicationTypes.stream()
        .anyMatch(appType -> appType == pwaApplicationType);
    if (!allowed) {
      throw new AccessDeniedException(
          String.format("Application type %s is not allowed to access this endpoint", pwaApplicationType.name()));
    }
  }

}
