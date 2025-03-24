package uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.controller;

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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.OptionsTemplateForm;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.OptionsTemplateService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/options-template")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = { PwaApplicationType.OPTIONS_VARIATION })
public class OptionsTemplateController {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.OPTIONS_TEMPLATE;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final OptionsTemplateService optionsTemplateService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final String ogaOptionsTemplateLink;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public OptionsTemplateController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                   ControllerHelperService controllerHelperService,
                                   OptionsTemplateService optionsTemplateService,
                                   PwaApplicationRedirectService pwaApplicationRedirectService,
                                   @Value("${oga.options.template.link}") String ogaOptionsTemplateLink,
                                   PadFileManagementService padFileManagementService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.optionsTemplateService = optionsTemplateService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.ogaOptionsTemplateLink = ogaOptionsTemplateLink;
    this.padFileManagementService = padFileManagementService;
  }

  private ModelAndView getModelAndView(PwaApplicationContext applicationContext,
                                       OptionsTemplateForm form) {
    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        applicationContext.getApplicationDetail(),
        DOCUMENT_TYPE
    );

    var modelAndView = new ModelAndView("pwaApplication/options/optionsTemplate")
        .addObject("ogaOptionsTemplateLink", ogaOptionsTemplateLink)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView, "Options template");

    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderOptionsTemplate(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") OptionsTemplateForm form,
                                            AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    padFileManagementService.mapFilesToForm(form, detail, DOCUMENT_TYPE);
    return getModelAndView(applicationContext, form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postOptionsTemplate(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaApplicationContext applicationContext,
                                          @ModelAttribute("form") OptionsTemplateForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount user,
                                          ValidationType validationType) {

    var detail = applicationContext.getApplicationDetail();
    bindingResult = optionsTemplateService.validate(form, bindingResult, validationType, detail);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getModelAndView(applicationContext, form), () -> {
      padFileManagementService.saveFiles(form, detail, DOCUMENT_TYPE);
      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
    });
  }
}
