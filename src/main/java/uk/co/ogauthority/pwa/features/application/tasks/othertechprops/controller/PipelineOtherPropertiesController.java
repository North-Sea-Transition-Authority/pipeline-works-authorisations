package uk.co.ogauthority.pwa.features.application.tasks.othertechprops.controller;

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
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.OtherPipelineProperty;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipeline-other-properties")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class PipelineOtherPropertiesController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPipelineOtherPropertiesService padPipelineOtherPropertiesService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PipelineOtherPropertiesController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                           PwaApplicationRedirectService pwaApplicationRedirectService,
                                           PadPipelineOtherPropertiesService padPipelineOtherPropertiesService,
                                           ControllerHelperService controllerHelperService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPipelineOtherPropertiesService = padPipelineOtherPropertiesService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderAddPipelineOtherProperties(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PipelineOtherPropertiesForm form) {
    var entities = padPipelineOtherPropertiesService.getPipelineOtherPropertyEntities(applicationContext.getApplicationDetail());
    padPipelineOtherPropertiesService.mapEntitiesToForm(form, entities, applicationContext.getApplicationDetail());
    return getAddPipelineOtherPropertiesModelAndView(applicationContext.getApplicationDetail());
  }


  @PostMapping
  public ModelAndView postAddPipelineOtherProperties(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PipelineOtherPropertiesForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padPipelineOtherPropertiesService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddPipelineOtherPropertiesModelAndView(applicationContext.getApplicationDetail()), () -> {
          var entities = padPipelineOtherPropertiesService.getPipelineOtherPropertyEntities(applicationContext.getApplicationDetail());
          padPipelineOtherPropertiesService.saveEntitiesUsingForm(form, entities, applicationContext.getApplicationDetail());
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });
  }




  private ModelAndView getAddPipelineOtherPropertiesModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinetechinfo/pipelineOtherProperties");
    modelAndView.addObject("properties", OtherPipelineProperty.asList(pwaApplicationDetail.getResourceType()))
        .addObject("propertyAvailabilityOptions", PropertyAvailabilityOption.asList())
        .addObject("propertyPhases", PropertyPhase.asList(pwaApplicationDetail.getResourceType()))
        .addObject("resourceType", pwaApplicationDetail.getResourceType().name());

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Other properties");
    return modelAndView;
  }





}
