package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo;

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
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.FluidCompositionInfoForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/fluid-composition")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class FluidCompositionInfoController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadFluidCompositionInfoService padFluidCompositionInfoService;

  @Autowired
  public FluidCompositionInfoController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                        PwaApplicationRedirectService pwaApplicationRedirectService,
                                        PadFluidCompositionInfoService padFluidCompositionInfoService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padFluidCompositionInfoService = padFluidCompositionInfoService;
  }


  @GetMapping
  public ModelAndView renderAddFluidCompositionInfo(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") FluidCompositionInfoForm form) {
    return getAddFluidCompositionInfoModelAndView(applicationContext.getApplicationDetail());
  }


  @PostMapping
  public ModelAndView postAddFluidCompositionInfo(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") FluidCompositionInfoForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padFluidCompositionInfoService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddFluidCompositionInfoModelAndView(applicationContext.getApplicationDetail()), () ->
            pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication()));
  }




  private ModelAndView getAddFluidCompositionInfoModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinetechinfo/fluidCompositionForm");
    modelAndView.addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()))
        .addObject("chemicals", Chemical.asList());

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Fluid composition");
    return modelAndView;
  }





}