package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.ProjectInformationValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/project-information")
@ApplicationTypeRestriction({
    PwaApplicationType.INITIAL,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class ProjectInformationController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ProjectInformationValidator validator;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public ProjectInformationController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                      PwaApplicationDetailService pwaApplicationDetailService,
                                      PwaApplicationRedirectService pwaApplicationRedirectService,
                                      ProjectInformationValidator validator,
                                      PadProjectInformationService padProjectInformationService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.validator = validator;
    this.padProjectInformationService = padProjectInformationService;
  }

  private ModelAndView getProjectInformationModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/projectInformation");
    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Project information");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderProjectInformation(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               @ModelAttribute("form") ProjectInformationForm form,
                                               AuthenticatedUserAccount user) {
    ensureAllowed(pwaApplicationType);
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail -> {
      var entity = padProjectInformationService.getPadProjectInformationData(detail);
      padProjectInformationService.mapEntityToForm(entity, form);
      return getProjectInformationModelAndView(detail);
    });
  }

  @PostMapping(params = "Save and complete later")
  public ModelAndView postContinueProjectInformation(@PathVariable("applicationType")
                                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                     @PathVariable("applicationId") Integer applicationId,
                                                     @Validated({ProjectInformationForm.Partial.class})
                                                     @ModelAttribute("form") ProjectInformationForm form,
                                                     BindingResult bindingResult,
                                                     AuthenticatedUserAccount user) {
    ensureAllowed(pwaApplicationType);
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        ControllerUtils.validateAndRedirect(bindingResult, getProjectInformationModelAndView(detail), () -> {
          var entity = padProjectInformationService.getPadProjectInformationData(detail);
          padProjectInformationService.saveEntityUsingForm(entity, form);
          return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
        })
    );
  }

  @PostMapping(params = "Complete")
  public ModelAndView postCompleteProjectInformation(@PathVariable("applicationType")
                                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                     @PathVariable("applicationId") Integer applicationId,
                                                     @Validated({ProjectInformationForm.Full.class})
                                                     @ModelAttribute("form") ProjectInformationForm form,
                                                     BindingResult bindingResult,
                                                     AuthenticatedUserAccount user) {
    ensureAllowed(pwaApplicationType);
    validator.validate(form, bindingResult);
    return pwaApplicationDetailService.withDraftTipDetail(applicationId, user, detail ->
        ControllerUtils.validateAndRedirect(bindingResult, getProjectInformationModelAndView(detail), () -> {
          var entity = padProjectInformationService.getPadProjectInformationData(detail);
          padProjectInformationService.saveEntityUsingForm(entity, form);
          return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
        })
    );
  }

  private void ensureAllowed(PwaApplicationType pwaApplicationType) {
    var allowed = Arrays.stream(this.getClass()
        .getAnnotation(ApplicationTypeRestriction.class)
        .value())
        .anyMatch(type -> type == pwaApplicationType);
    if (!allowed) {
      throw new AccessDeniedException(
          String.format("Application type %s is not allowed to access this endpoint", pwaApplicationType.name()));
    }
  }

}
