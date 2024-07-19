package uk.co.ogauthority.pwa.features.feemanagement.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.features.feemanagement.display.FeePeriodDisplayService;
import uk.co.ogauthority.pwa.features.feemanagement.service.FeePeriodService;
import uk.co.ogauthority.pwa.features.feemanagement.service.FeePeriodValidator;
import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.util.FlashUtils;

@Controller
@RequestMapping("/fee-management")
public class FeeManagementController {

  private final FeePeriodDisplayService displayService;

  private final FeePeriodValidator validator;

  private final ControllerHelperService helperService;

  private final FeePeriodService feePeriodService;

  private static final Logger LOGGER = LoggerFactory.getLogger(FeeManagementController.class);

  private static final String ROUTE_OVERVIEW_URL = ReverseRouter.route(on(FeeManagementController.class).renderFeeManagementOverview(null));

  @Autowired
  public FeeManagementController(FeePeriodDisplayService displayService,
                                 FeePeriodValidator validator,
                                 ControllerHelperService helperService,
                                 FeePeriodService feePeriodService) {
    this.displayService = displayService;
    this.validator = validator;
    this.helperService = helperService;
    this.feePeriodService = feePeriodService;
  }


  @GetMapping
  public ModelAndView renderFeeManagementOverview(AuthenticatedUserAccount authenticatedUser) {
    checkUserPrivilege(authenticatedUser);
    return new ModelAndView("fees/management/feeManagement")
        .addObject("feePeriods", displayService.listAllPeriods())
        .addObject("newPeriodUrl", ReverseRouter.route(on(
            FeeManagementController.class).renderNewPeriodForm(null,
            new FeePeriodForm())))
        .addObject("editPeriodUrl", ReverseRouter.route(on(
            FeeManagementController.class).renderEditPeriodForm(null,
            null,
            new FeePeriodForm())))
        .addObject("urlFactory", new FeeManagementUrlFactory())
        .addObject("createPeriodFlag", displayService.futurePeriodExists())
        .addObject("success");
  }

  @GetMapping("/period")
  public ModelAndView renderFeePeriodDetail(AuthenticatedUserAccount authenticatedUser, Integer periodId) {
    checkUserPrivilege(authenticatedUser);
    var feePeriodOptional = displayService.findPeriodById(periodId);
    if (feePeriodOptional.isEmpty()) {
      String errorMessage = String.format("No fee period found for id %s", periodId);
      LOGGER.error(errorMessage);
      throw new EntityNotFoundException(errorMessage);
    }

    return new ModelAndView("fees/management/feeDetail")
        .addObject("backUrl", ReverseRouter.route(on(FeeManagementController.class)
            .renderFeeManagementOverview(authenticatedUser)))
        .addObject("feePeriod", feePeriodOptional.get())
        .addObject("feeMap", displayService.getFeesByPeriodId(periodId));
  }

  @GetMapping("/new")
  public ModelAndView renderNewPeriodForm(AuthenticatedUserAccount authenticatedUser,
                                          @ModelAttribute("form") FeePeriodForm form) {
    checkUserPrivilege(authenticatedUser);
    if (feePeriodService.pendingPeriodExists()) {
      return ReverseRouter.redirect(on(FeeManagementController.class)
          .renderFeeManagementOverview(authenticatedUser));
    }

    return new ModelAndView("fees/form/newFeePeriod")
        .addObject("applicationTypes", PwaApplicationType.values())
        .addObject("applicationFeeTypes", PwaApplicationFeeType.values())
        .addObject("cancelUrl", ROUTE_OVERVIEW_URL);
  }

  @PostMapping("/new")
  public ModelAndView postNewPeriodForm(AuthenticatedUserAccount authenticatedUser,
                                        @ModelAttribute("form") FeePeriodForm form,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {

    checkUserPrivilege(authenticatedUser);
    if (feePeriodService.pendingPeriodExists()) {
      LOGGER.debug("Only one pending fee period allowed at any one time - rerouting to management screen");
      return ReverseRouter.redirect(on(FeeManagementController.class)
          .renderFeeManagementOverview(authenticatedUser));
    }

    var modelAndView = renderNewPeriodForm(authenticatedUser, form);
    validator.validate(form, bindingResult);

    return helperService.checkErrorsAndRedirect(bindingResult,
        modelAndView,
        () -> {
          feePeriodService.saveFeePeriod(form, authenticatedUser.getLinkedPerson());
          FlashUtils.success(redirectAttributes, "Success", "Period created successfully");
          return ReverseRouter.redirect(on(FeeManagementController.class)
              .renderFeeManagementOverview(null));
        });
  }

  @GetMapping("/edit/{periodId}")
  public ModelAndView renderEditPeriodForm(AuthenticatedUserAccount authenticatedUser,
                                           @PathVariable Integer periodId,
                                           @ModelAttribute("form") FeePeriodForm form) {

    checkUserPrivilege(authenticatedUser);
    displayService.populatePeriodFormForEdit(form, periodId);
    return createEditPeriodFormModelAndView(periodId, form);

  }

  private ModelAndView createEditPeriodFormModelAndView(Integer periodId, FeePeriodForm form) {

    return new ModelAndView("fees/form/editFeePeriod")
        .addObject("feePeriod", displayService.findPeriodById(periodId))
        .addObject("applicationTypes", PwaApplicationType.values())
        .addObject("applicationFeeTypes", PwaApplicationFeeType.values())
        .addObject("cancelUrl", ROUTE_OVERVIEW_URL);

  }

  @PostMapping("/edit/{periodId}")
  public ModelAndView postEditPeriodForm(AuthenticatedUserAccount authenticatedUser,
                                         @PathVariable Integer periodId,
                                         @ModelAttribute("form") FeePeriodForm form,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes) {

    checkUserPrivilege(authenticatedUser);
    validator.validate(form, bindingResult);
    var modelAndView = createEditPeriodFormModelAndView(periodId, form);

    return helperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {
      feePeriodService.saveFeePeriod(form, authenticatedUser.getLinkedPerson());
      FlashUtils.success(redirectAttributes, "Success", "Period edited successfully");
      return ReverseRouter.redirect(on(FeeManagementController.class).renderFeeManagementOverview(null));
    });
  }

  private void checkUserPrivilege(AuthenticatedUserAccount authenticatedUser) {
    if (!authenticatedUser.hasPrivilege(PwaUserPrivilege.PWA_MANAGER)) {
      throw new AccessDeniedException("Access to fee management denied");
    }
  }
}
