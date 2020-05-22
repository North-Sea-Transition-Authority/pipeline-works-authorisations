package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
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

  @Autowired
  public PermanentDepositController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                    PwaApplicationRedirectService pwaApplicationRedirectService,
                                    PermanentDepositService permanentDepositService,
                                    PwaApplicationFileService applicationFileService,
                                    PadPipelineService padPipelineService) {
    this.applicationFileService = applicationFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.permanentDepositService = permanentDepositService;
    this.padPipelineService = padPipelineService;
  }


  @GetMapping
  public ModelAndView renderPermanentDepositsOverview(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PermanentDepositsForm form) {
    return getOverviewPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @GetMapping("/add-deposits")
  public ModelAndView renderAddPermanentDeposits(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              @PathVariable("applicationId") Integer applicationId,
                                              PwaApplicationContext applicationContext,
                                              @ModelAttribute("form") PermanentDepositsForm form) {
    return getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), form, ScreenActionType.ADD);
  }

  @GetMapping("/edit-deposits/{depositId}")
  public ModelAndView renderEditPermanentDeposits(@PathVariable("applicationType")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @PathVariable("applicationId") Integer applicationId,
                                                  @PathVariable("depositId") Integer entityId,
                                                 PwaApplicationContext applicationContext,
                                                 @ModelAttribute("form") PermanentDepositsForm form) {
    permanentDepositService.mapEntityToFormById(entityId, form);
    return getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), form, ScreenActionType.EDIT);
  }

  @PostMapping
  public ModelAndView postPermanentDepositsOverview(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PermanentDepositsForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {
    return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
  }

  @PostMapping("/add-deposits")
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
        getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), form, ScreenActionType.ADD), () -> {
          permanentDepositService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return ReverseRouter.redirect(on(PermanentDepositController.class).renderPermanentDepositsOverview(
              pwaApplicationType, applicationId, null, null));
        });

  }

  @PostMapping("/edit-deposits/{depositId}")
  public ModelAndView postEditPermanentDeposits(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("depositId") Integer entityId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PermanentDepositsForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    form.setEntityID(entityId);
    bindingResult = permanentDepositService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), form, ScreenActionType.EDIT), () -> {
          permanentDepositService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return ReverseRouter.redirect(on(PermanentDepositController.class).renderPermanentDepositsOverview(
              pwaApplicationType, applicationId, null, null));
        });

  }



  private ModelAndView getOverviewPermanentDepositsModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                                PermanentDepositsForm form) {
    var modelAndView = new ModelAndView("pwaApplication/shared/permanentdeposits/permanentDepositsView");
    modelAndView.addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()))
        .addObject("deposits", permanentDepositService.getPermanentDepositViewForms(pwaApplicationDetail))
        .addObject("addDepositUrl", ReverseRouter.route(on(PermanentDepositController.class)
            .renderAddPermanentDeposits(
                pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null, null)))
        .addObject("editDepositUrls", permanentDepositService.getEditUrlsForDeposits(pwaApplicationDetail));

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Permanent deposits");
    return modelAndView;
  }


  private ModelAndView getAddEditPermanentDepositsModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                        PermanentDepositsForm form, ScreenActionType type) {
    var modelAndView = new ModelAndView("pwaApplication/shared/permanentdeposits/permanentDepositsForm");
    modelAndView.addObject("pipelines", padPipelineService.getPipelines(pwaApplicationDetail))
        .addObject("materialTypes", MaterialType.asList())
        .addObject("longDirections", LongitudeDirection.stream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("backUrl", ReverseRouter.route(on(PermanentDepositController.class)
            .renderPermanentDepositsOverview(
                pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null, null)))
        .addObject("screenAction", type);

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Permanent deposits");
    return modelAndView;
  }




}