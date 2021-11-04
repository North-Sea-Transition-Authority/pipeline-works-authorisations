package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.FluidCompositionForm;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/fluid-composition")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class FluidCompositionInfoController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadFluidCompositionInfoService padFluidCompositionInfoService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public FluidCompositionInfoController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                        PwaApplicationRedirectService pwaApplicationRedirectService,
                                        PadFluidCompositionInfoService padFluidCompositionInfoService,
                                        ControllerHelperService controllerHelperService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padFluidCompositionInfoService = padFluidCompositionInfoService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderAddFluidCompositionInfo(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") FluidCompositionForm form) {
    var entities = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(applicationContext.getApplicationDetail());
    padFluidCompositionInfoService.mapEntitiesToForm(form, entities);
    return getAddFluidCompositionInfoModelAndView(applicationContext.getApplicationDetail());
  }


  @PostMapping
  public ModelAndView postAddFluidCompositionInfo(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") FluidCompositionForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padFluidCompositionInfoService.validate(form, bindingResult, validationType, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddFluidCompositionInfoModelAndView(applicationContext.getApplicationDetail()), () -> {
          var entities = padFluidCompositionInfoService.getPadFluidCompositionInfoEntities(applicationContext.getApplicationDetail());
          padFluidCompositionInfoService.saveEntitiesUsingForm(form, entities);
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });

  }




  private ModelAndView getAddFluidCompositionInfoModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinetechinfo/fluidCompositionForm");
    modelAndView.addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()))
        .addObject("chemicals", Chemical.asList())
        .addObject("fluidCompositionOptions", FluidCompositionOption.asList());

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Fluid composition");
    return modelAndView;
  }





}