package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/permanent-deposits")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class PermanentDepositController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PermanentDepositService permanentDepositService;
  private final PwaApplicationFileService applicationFileService;
  private final PadPipelineService padPipelineService;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public PermanentDepositController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                    PwaApplicationRedirectService pwaApplicationRedirectService,
                                    PermanentDepositService permanentDepositService,
                                    PwaApplicationFileService applicationFileService,
                                    PadPipelineService padPipelineService,
                                    PadProjectInformationService padProjectInformationService) {
    this.applicationFileService = applicationFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.permanentDepositService = permanentDepositService;
    this.padPipelineService = padPipelineService;
    this.padProjectInformationService = padProjectInformationService;
  }

  @GetMapping
  public ModelAndView renderPermanentDeposits(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               PwaApplicationContext applicationContext,
                                               @ModelAttribute("form") PermanentDepositsForm form) {
    return getPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  public ModelAndView postPermanentDeposits(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") Integer applicationId,
                                             PwaApplicationContext applicationContext,
                                             @ModelAttribute("form") PermanentDepositsForm form,
                                             BindingResult bindingResult,
                                             ValidationType validationType) {

    bindingResult = permanentDepositService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
            getPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), form), () -> {
          permanentDepositService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });

  }


  private ModelAndView getPermanentDepositsModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                         PermanentDepositsForm form) {
    var modelAndView = new ModelAndView("pwaApplication/shared/permanentdeposits/permanentDeposits");
    modelAndView.addObject("pipelines", padPipelineService.getPipelines(pwaApplicationDetail))
        .addObject("materialTypes", MaterialType.asList())
        .addObject("longDirections", LongitudeDirection.stream()
          .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("proposedStartDate", padProjectInformationService.getProposedStartDate(pwaApplicationDetail))
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()));

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Permanent deposits");
    return modelAndView;
  }

}
