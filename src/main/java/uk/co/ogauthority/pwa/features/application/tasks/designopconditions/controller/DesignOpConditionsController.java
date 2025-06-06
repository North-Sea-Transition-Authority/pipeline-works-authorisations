package uk.co.ogauthority.pwa.features.application.tasks.designopconditions.controller;

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
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/design-operating-conditions")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class DesignOpConditionsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadDesignOpConditionsService padDesignOpConditionsService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public DesignOpConditionsController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                      PwaApplicationRedirectService pwaApplicationRedirectService,
                                      PadDesignOpConditionsService padDesignOpConditionsService,
                                      ControllerHelperService controllerHelperService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padDesignOpConditionsService = padDesignOpConditionsService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderAddDesignOpConditions(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") DesignOpConditionsForm form) {
    var entity = padDesignOpConditionsService.getDesignOpConditionsEntity(applicationContext.getApplicationDetail());
    padDesignOpConditionsService.mapEntityToForm(form, entity);
    return getAddDesignOpConditionsModelAndView(applicationContext.getApplicationDetail());
  }


  @PostMapping
  public ModelAndView postAddDesignOpConditions(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") DesignOpConditionsForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padDesignOpConditionsService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddDesignOpConditionsModelAndView(applicationContext.getApplicationDetail()), () -> {
          var entity = padDesignOpConditionsService.getDesignOpConditionsEntity(applicationContext.getApplicationDetail());
          padDesignOpConditionsService.saveEntityUsingForm(form, entity);
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });
  }




  private ModelAndView getAddDesignOpConditionsModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinetechinfo/designOpConditionsForm")
        .addObject("unitMeasurements", UnitMeasurement.toMap())
        .addObject("resourceType", pwaApplicationDetail.getResourceType().name());

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Design and operating conditions");
    return modelAndView;
  }





}
