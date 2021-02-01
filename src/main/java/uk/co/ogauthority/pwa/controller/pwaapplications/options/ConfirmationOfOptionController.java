package uk.co.ogauthority.pwa.controller.pwaapplications.options;

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
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.ConfirmOptionForm;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadConfirmationOfOptionService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/confirm-option")
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {PwaApplicationType.OPTIONS_VARIATION})
public class ConfirmationOfOptionController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PadConfirmationOfOptionService padConfirmationOfOptionService;
  private final ControllerHelperService controllerHelperService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public ConfirmationOfOptionController(ApplicationBreadcrumbService breadcrumbService,
                                        PadConfirmationOfOptionService padConfirmationOfOptionService,
                                        ControllerHelperService controllerHelperService,
                                        PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.breadcrumbService = breadcrumbService;
    this.padConfirmationOfOptionService = padConfirmationOfOptionService;
    this.controllerHelperService = controllerHelperService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  @GetMapping
  public ModelAndView renderConfirmOption(@PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          @PathVariable("applicationId") Integer applicationId,
                                          PwaApplicationContext applicationContext,
                                          @ModelAttribute("form") ConfirmOptionForm form) {

    var padConfirmationOfOptionOpt = padConfirmationOfOptionService.findPadConfirmationOfOption(
        applicationContext.getApplicationDetail());
    padConfirmationOfOptionOpt.ifPresent(
        padConfirmationOfOption -> padConfirmationOfOptionService.mapEntityToForm(form, padConfirmationOfOption)
    );

    var modelAndView = createModelAndView(applicationContext);

    return modelAndView;
  }

  @PostMapping
  public ModelAndView confirmOption(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") Integer applicationId,
                                    PwaApplicationContext applicationContext,
                                    @ModelAttribute("form") ConfirmOptionForm form,
                                    BindingResult bindingResult,
                                    ValidationType validationType) {

    bindingResult = padConfirmationOfOptionService.validate(form, bindingResult, validationType,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        createModelAndView(applicationContext),
        () -> {
          var padConfirmationOfOption = padConfirmationOfOptionService.getOrCreatePadConfirmationOfOption(
              applicationContext.getApplicationDetail()
          );
          padConfirmationOfOptionService.mapFormToEntity(form, padConfirmationOfOption);
          padConfirmationOfOptionService.savePadConfirmation(padConfirmationOfOption);
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        }
    );

  }

  private ModelAndView createModelAndView(PwaApplicationContext applicationContext) {
    var modelAndView = new ModelAndView("pwaApplication/options/confirmOptionForm")
        .addObject("pageHeading", ApplicationTask.CONFIRM_OPTIONS.getDisplayName())
        .addObject("confirmOptionList", ConfirmedOptionType.orderedStream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, ConfirmedOptionType::getDisplayName)));

    breadcrumbService.fromTaskList(
        applicationContext.getPwaApplication(),
        modelAndView,
        ApplicationTask.CONFIRM_OPTIONS.getDisplayName()
    );

    return modelAndView;

  }

}
