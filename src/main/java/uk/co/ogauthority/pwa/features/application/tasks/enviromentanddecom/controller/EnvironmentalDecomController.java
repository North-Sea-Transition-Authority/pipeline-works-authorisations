package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.controller;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.DecommissioningCondition;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalCondition;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/env-decom")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class EnvironmentalDecomController {

  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public EnvironmentalDecomController(
      PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      ControllerHelperService controllerHelperService) {
    this.padEnvironmentalDecommissioningService = padEnvironmentalDecommissioningService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.controllerHelperService = controllerHelperService;
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
            .collect(StreamUtils.toLinkedHashMap(Enum::name, DecommissioningCondition::getConditionText)))
        .addObject("availableQuestions", padEnvironmentalDecommissioningService.getAvailableQuestions(pwaApplicationDetail));

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Environmental and decommissioning");

    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderEnvDecom(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     PwaApplicationContext applicationContext,
                                     @ModelAttribute("form") EnvironmentalDecommissioningForm form) {
    var detail = applicationContext.getApplicationDetail();
    var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
    var modelAndView = getEnvDecomModelAndView(detail);
    padEnvironmentalDecommissioningService.mapEntityToForm(envDecomData, form);
    return modelAndView;
  }

  @PostMapping
  public ModelAndView postEnvDecom(@PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   PwaApplicationContext applicationContext,
                                   @ModelAttribute("form") EnvironmentalDecommissioningForm form,
                                   BindingResult bindingResult,
                                   ValidationType validationType) {

    var detail = applicationContext.getApplicationDetail();
    bindingResult = padEnvironmentalDecommissioningService.validate(
        form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getEnvDecomModelAndView(detail), () -> {
      var envDecomData = padEnvironmentalDecommissioningService.getEnvDecomData(detail);
      padEnvironmentalDecommissioningService.saveEntityUsingForm(envDecomData, form);
      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
    });

  }

}
