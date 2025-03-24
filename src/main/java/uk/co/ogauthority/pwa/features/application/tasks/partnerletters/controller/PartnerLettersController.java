package uk.co.ogauthority.pwa.features.application.tasks.partnerletters.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/partner-letters")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class PartnerLettersController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPartnerLettersService padPartnerLettersService;
  private final ControllerHelperService controllerHelperService;
  private final String partnerLettersTemplateLink;

  private final PadFileManagementService padFileManagementService;

  @Autowired
  public PartnerLettersController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                  PwaApplicationRedirectService pwaApplicationRedirectService,
                                  PadPartnerLettersService padPartnerLettersService,
                                  ControllerHelperService controllerHelperService,
                                  @Value("${oga.partnerletters.template.link}") String partnerLettersTemplateLink,
                                  PadFileManagementService padFileManagementService
  ) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPartnerLettersService = padPartnerLettersService;
    this.controllerHelperService = controllerHelperService;
    this.partnerLettersTemplateLink = partnerLettersTemplateLink;
    this.padFileManagementService = padFileManagementService;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderAddPartnerLetters(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              @PathVariable("applicationId") Integer applicationId,
                                              PwaApplicationContext applicationContext,
                                              @ModelAttribute("form") PartnerLettersForm form) {
    padPartnerLettersService.mapEntityToForm(applicationContext.getApplicationDetail(), form);
    return getAddPartnerLettersModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postAddPartnerLetters(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PartnerLettersForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padPartnerLettersService.validate(form, bindingResult, validationType, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddPartnerLettersModelAndView(applicationContext.getApplicationDetail(), form), () -> {
          padPartnerLettersService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });

  }

  private ModelAndView getAddPartnerLettersModelAndView(PwaApplicationDetail pwaApplicationDetail, PartnerLettersForm form) {
    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        pwaApplicationDetail,
        FileDocumentType.PARTNER_LETTERS
    );

    var modelAndView = new ModelAndView("pwaApplication/shared/partnerletters/partnerLetters")
        .addObject("partnerLettersTemplateLink", partnerLettersTemplateLink)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView, "Partner approval letters");

    return modelAndView;

  }

}