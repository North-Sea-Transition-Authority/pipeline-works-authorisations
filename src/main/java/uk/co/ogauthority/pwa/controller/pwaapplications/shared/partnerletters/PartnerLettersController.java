package uk.co.ogauthority.pwa.controller.pwaapplications.shared.partnerletters;

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
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/partner-letters")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class PartnerLettersController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPartnerLettersService padPartnerLettersService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PartnerLettersController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                  PwaApplicationRedirectService pwaApplicationRedirectService,
                                  PadPartnerLettersService padPartnerLettersService,
                                  ControllerHelperService controllerHelperService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPartnerLettersService = padPartnerLettersService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderAddPartnerLetters(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PartnerLettersForm form) {
    padPartnerLettersService.mapEntityToForm(applicationContext.getApplicationDetail(), form);
    return getAddPartnerLettersModelAndView(applicationContext.getApplicationDetail());
  }


  @PostMapping
  public ModelAndView postAddPartnerLetters(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PartnerLettersForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padPartnerLettersService.validate(form, bindingResult, validationType, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddPartnerLettersModelAndView(applicationContext.getApplicationDetail()), () -> {
          padPartnerLettersService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form);
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });

  }




  private ModelAndView getAddPartnerLettersModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/partnerletters/partnerLetters");

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Partner approval letters");
    return modelAndView;
  }





}