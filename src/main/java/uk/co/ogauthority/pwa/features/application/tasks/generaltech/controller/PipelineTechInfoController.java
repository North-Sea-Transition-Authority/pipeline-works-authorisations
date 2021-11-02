package uk.co.ogauthority.pwa.features.application.tasks.generaltech.controller;

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
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/general-tech-details")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class PipelineTechInfoController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPipelineTechInfoService padPipelineTechInfoService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PipelineTechInfoController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                    PwaApplicationRedirectService pwaApplicationRedirectService,
                                    PadPipelineTechInfoService padPipelineTechInfoService,
                                    ControllerHelperService controllerHelperService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPipelineTechInfoService = padPipelineTechInfoService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderAddPipelineTechInfo(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PipelineTechInfoForm form) {
    var entity = padPipelineTechInfoService.getPipelineTechInfoEntity(applicationContext.getApplicationDetail());
    padPipelineTechInfoService.mapEntityToForm(form, entity);
    return getAddPipelineTechInfoModelAndView(applicationContext.getApplicationDetail());
  }


  @PostMapping
  public ModelAndView postAddPipelineTechInfo(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PipelineTechInfoForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padPipelineTechInfoService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddPipelineTechInfoModelAndView(applicationContext.getApplicationDetail()), () -> {
          var entity = padPipelineTechInfoService.getPipelineTechInfoEntity(applicationContext.getApplicationDetail());
          padPipelineTechInfoService.saveEntityUsingForm(form, entity);
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });
  }




  private ModelAndView getAddPipelineTechInfoModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinetechinfo/pipelineTechInfoForm");
    modelAndView.addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()));

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "General technical details");
    return modelAndView;
  }





}