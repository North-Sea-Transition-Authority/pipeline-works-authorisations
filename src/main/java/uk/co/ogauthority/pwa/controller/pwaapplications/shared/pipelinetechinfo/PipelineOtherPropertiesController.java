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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineOtherPropertiesForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipeline-other-properties")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class PipelineOtherPropertiesController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPipelineOtherPropertiesService padPipelineOtherPropertiesService;

  @Autowired
  public PipelineOtherPropertiesController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                           PwaApplicationRedirectService pwaApplicationRedirectService,
                                           PadPipelineOtherPropertiesService padPipelineOtherPropertiesService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPipelineOtherPropertiesService = padPipelineOtherPropertiesService;
  }


  @GetMapping
  public ModelAndView renderAddPipelineOtherProperties(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PipelineOtherPropertiesForm form) {
    var entity = padPipelineOtherPropertiesService.getPipelineOtherPropertiesEntity(applicationContext.getApplicationDetail());
    padPipelineOtherPropertiesService.mapEntityToForm(form, entity);
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

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddPipelineOtherPropertiesModelAndView(applicationContext.getApplicationDetail()), () -> {
          var entity = padPipelineOtherPropertiesService.getPipelineOtherPropertiesEntity(applicationContext.getApplicationDetail());
          padPipelineOtherPropertiesService.saveEntityUsingForm(form, entity);
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });
  }




  private ModelAndView getAddPipelineOtherPropertiesModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinetechinfo/pipelineOtherProperties");

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Other properties");
    return modelAndView;
  }





}