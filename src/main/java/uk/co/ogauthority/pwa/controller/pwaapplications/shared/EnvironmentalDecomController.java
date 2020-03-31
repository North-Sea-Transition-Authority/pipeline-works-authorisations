package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.util.Comparator;
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
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvDecomForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.EnvDecomValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/env-decom")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT
})
public class EnvironmentalDecomController {

  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private final EnvDecomValidator validator;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public EnvironmentalDecomController(
      PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService,
      EnvDecomValidator validator,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.padEnvironmentalDecommissioningService = padEnvironmentalDecommissioningService;
    this.validator = validator;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getEnvDecomModelAndView(PwaApplicationDetail pwaApplicationDetail) {
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
    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Environmental and decommissioning");
    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderEnvDecom(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     PwaApplicationContext applicationContext,
                                     @ModelAttribute("form") EnvDecomForm form,
                                     AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
    var modelAndView = getEnvDecomModelAndView(detail);
    padEnvironmentalDecommissioningService.mapEntityToForm(envDecomData, form);
    return modelAndView;
  }

  @PostMapping(params = "Complete")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postCompleteEnvDecom(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaApplicationContext applicationContext,
                                           @Valid @ModelAttribute("form") EnvDecomForm form,
                                           BindingResult bindingResult,
                                           AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();

    validator.validate(form, bindingResult);
    return ControllerUtils.validateAndRedirect(bindingResult, getEnvDecomModelAndView(detail), () -> {
      var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
      padEnvironmentalDecommissioningService.saveEntityUsingForm(envDecomData, form);
      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
    });

  }

  @PostMapping(params = "Save and complete later")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postContinueEnvDecom(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaApplicationContext applicationContext,
                                           @ModelAttribute("form") EnvDecomForm form,
                                           BindingResult bindingResult,
                                           AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();

    return ControllerUtils.validateAndRedirect(bindingResult, getEnvDecomModelAndView(detail), () -> {
      var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
      padEnvironmentalDecommissioningService.saveEntityUsingForm(envDecomData, form);
      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
    });

  }

}
