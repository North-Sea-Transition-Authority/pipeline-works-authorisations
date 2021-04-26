package uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits;

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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;



@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/permanent-deposits")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
public class PermanentDepositController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PermanentDepositService permanentDepositService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PermanentDepositController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                    PwaApplicationRedirectService pwaApplicationRedirectService,
                                    PermanentDepositService permanentDepositService,
                                    ControllerHelperService controllerHelperService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.permanentDepositService = permanentDepositService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderPermanentDepositsOverview(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") Integer applicationId,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PermanentDepositsForm form) {
    return getOverviewPermanentDepositsModelAndView(applicationContext.getApplicationDetail());
  }

  @GetMapping("/add-deposits")
  public ModelAndView renderAddPermanentDeposits(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              @PathVariable("applicationId") Integer applicationId,
                                              PwaApplicationContext applicationContext,
                                              @ModelAttribute("form") PermanentDepositsForm form) {
    return getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(),  ScreenActionType.ADD);
  }

  @GetMapping("/edit-deposits/{depositId}")
  public ModelAndView renderEditPermanentDeposits(@PathVariable("applicationType")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 @PathVariable("applicationId") Integer applicationId,
                                                  @PathVariable("depositId") Integer depositId,
                                                 PwaApplicationContext applicationContext,
                                                 @ModelAttribute("form") PermanentDepositsForm form) {
    permanentDepositService.mapEntityToFormById(depositId, form);
    return getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.EDIT);
  }

  @GetMapping("/remove-deposit/{depositId}")
  public ModelAndView renderRemovePermanentDeposits(@PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  @PathVariable("applicationId") Integer applicationId,
                                                  @PathVariable("depositId") Integer depositId,
                                                  PwaApplicationContext applicationContext,
                                                  @ModelAttribute("form") PermanentDepositsForm form) {
    var permanentDepositOverview = permanentDepositService.createViewFromDepositId(depositId);
    return getRemovePermanentDepositsModelAndView(applicationContext.getApplicationDetail(), permanentDepositOverview);
  }

  @PostMapping
  public ModelAndView postPermanentDepositsOverview(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PermanentDepositsForm form,
                                            BindingResult bindingResult) {

    var depositSummaryValidationResult = permanentDepositService.getDepositSummaryScreenValidationResult(
        applicationContext.getApplicationDetail());
    if (!depositSummaryValidationResult.isSectionComplete()) {
      return getOverviewPermanentDepositsModelAndView(applicationContext.getApplicationDetail())
          .addObject("depositSummaryValidationResult", depositSummaryValidationResult);
    }
    return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
  }

  @PostMapping("/add-deposits")
  public ModelAndView postPermanentDeposits(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PermanentDepositsForm form,
                                            BindingResult bindingResult) {

    bindingResult = permanentDepositService.validate(form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.ADD), () -> {
          permanentDepositService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return ReverseRouter.redirect(on(PermanentDepositController.class).renderPermanentDepositsOverview(
              pwaApplicationType, applicationId, null, null));
        });

  }

  @PostMapping("/edit-deposits/{depositId}")
  public ModelAndView postEditPermanentDeposits(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("depositId") Integer depositId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PermanentDepositsForm form,
                                            BindingResult bindingResult) {

    form.setEntityID(depositId);
    bindingResult = permanentDepositService.validate(form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddEditPermanentDepositsModelAndView(applicationContext.getApplicationDetail(), ScreenActionType.EDIT), () -> {
          permanentDepositService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return ReverseRouter.redirect(on(PermanentDepositController.class).renderPermanentDepositsOverview(
              pwaApplicationType, applicationId, null, null));
        });

  }

  @PostMapping("/remove-deposit/{depositId}")
  public ModelAndView postRemovePermanentDeposits(@PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                @PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("depositId") Integer depositId,
                                                PwaApplicationContext applicationContext,
                                                @ModelAttribute("form") PermanentDepositsForm form,
                                                BindingResult bindingResult) {

    permanentDepositService.removeDeposit(depositId);
    return ReverseRouter.redirect(on(PermanentDepositController.class)
        .renderPermanentDepositsOverview(pwaApplicationType, applicationId, null, null));
  }


  private ModelAndView getOverviewPermanentDepositsModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var permanentDepositViews = permanentDepositService.getPermanentDepositViews(pwaApplicationDetail);
    var modelAndView = new ModelAndView("pwaApplication/shared/permanentdeposits/permanentDepositsView");
    modelAndView.addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()))
        .addObject("deposits", permanentDepositViews)
        .addObject("addDepositUrl", ReverseRouter.route(on(PermanentDepositController.class)
            .renderAddPermanentDeposits(
                pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null, null)))
        .addObject("editDepositUrls", permanentDepositService.getEditUrlsForDeposits(pwaApplicationDetail))
        .addObject("removeDepositUrls", permanentDepositService.getRemoveUrlsForDeposits(pwaApplicationDetail));

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Permanent deposits");
    return modelAndView;
  }


  private ModelAndView getAddEditPermanentDepositsModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                               ScreenActionType type) {

    var modelAndView = new ModelAndView("pwaApplication/shared/permanentdeposits/permanentDepositsForm");
    modelAndView.addObject("pipelines", permanentDepositService.getPipelinesMapForDeposits(pwaApplicationDetail))
        .addObject("materialTypes", MaterialType.asList())
        .addObject("longDirections", LongitudeDirection.stream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("backUrl", ReverseRouter.route(on(PermanentDepositController.class)
            .renderPermanentDepositsOverview(
                pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null, null)))
        .addObject("screenAction", type);

    applicationBreadcrumbService.fromDepositsOverview(pwaApplicationDetail.getPwaApplication(), modelAndView,
        type.getSubmitButtonText() + " deposit");
    return modelAndView;
  }


  private ModelAndView getRemovePermanentDepositsModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                              PermanentDepositOverview view) {
    var modelAndView = new ModelAndView("pwaApplication/shared/permanentdeposits/permanentDepositsRemove");
    modelAndView.addObject("deposit", view)
        .addObject("cancelUrl", ReverseRouter.route(on(PermanentDepositController.class)
            .renderPermanentDepositsOverview(
                pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),null, null)));

    applicationBreadcrumbService.fromDepositsOverview(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Remove deposit");
    return modelAndView;
  }



}