package uk.co.ogauthority.pwa.features.application.tasks.huoo.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.HuooSummaryValidationResult;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooSummaryView;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooSummaryViewService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadHuooValidationService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.StringDisplayUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/huoo")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.HUOO_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class HuooController {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuooController.class);

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadHuooValidationService padHuooValidationService;
  private final PadHuooSummaryViewService padHuooSummaryViewService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public HuooController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadHuooValidationService padHuooValidationService,
      PadHuooSummaryViewService padHuooSummaryViewService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padHuooValidationService = padHuooValidationService;
    this.padHuooSummaryViewService = padHuooSummaryViewService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getHuooModelAndView(PwaApplicationDetail pwaApplicationDetail) {

    PadHuooSummaryView padHuooSummaryView = padHuooSummaryViewService.getPadHuooSummaryView(pwaApplicationDetail);

    var modelAndView = new ModelAndView("pwaApplication/shared/huoo/overview")
        .addObject("addHuooUrl", ReverseRouter.route(on(AddHuooController.class)
            .renderAddHuoo(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null, null)))
        .addObject("huooOrgs", padHuooSummaryView.getHuooOrganisationUnitRoleViews())
        .addObject("treatyAgreements", padHuooSummaryView.getTreatyAgreementViews())
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()))
        .addObject("showHolderGuidance", padHuooSummaryView.canShowHolderGuidance());

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Holders, users, operators, and owners");

    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderHuooSummary(@PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("applicationId") Integer applicationId,
                                        PwaApplicationContext applicationContext,
                                        AuthenticatedUserAccount user) {
    return getHuooModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postHuooSummary(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") Integer applicationId,
                                      PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();

    var validationResult = padHuooValidationService.getHuooSummaryValidationResult(detail);

    if (!validationResult.isValid()) {
      var modelAndView = getHuooModelAndView(detail);

      if (!validationResult.getUnassignedRoles().isEmpty()) {
        return modelAndView.addObject("errorMessage", "You must have at least one holder, user, operator, and owner");
      }

      if (validationResult.getBreachedBusinessRules()
          .contains(HuooSummaryValidationResult.HuooRules.CANNOT_HAVE_TREATY_AND_PORTAL_ORG_USERS)) {
        return modelAndView.addObject("errorMessage", "You cannot define both a treaty agreement and legal entities as users");
      }

      if (!validationResult.getSortedInactiveOrganisationsWithRole().isEmpty()) {
        var inactiveOrgCvs = String.join(", ", validationResult.getSortedInactiveOrganisationsWithRole());
        var organisationPluralisedOrSingular = StringDisplayUtils.pluralise(
            "organisation", validationResult.getSortedInactiveOrganisationsWithRole().size()
        );

        return modelAndView.addObject(
            "errorMessage",
            String.format("You must replace the following inactive HUOO %s: %s", organisationPluralisedOrSingular, inactiveOrgCvs)
        );
      }

      // if we have reached here, we have not handled specifically an error condition. do a log and return something generic
      LOGGER.warn("Unhandled invalid HUOO summary state found. pad_id:[{}], huooResult:[{}]", detail.getId(), validationResult);
      return modelAndView.addObject(
          "errorMessage",
          "You must correct the invalid HUOO's");
    }

    return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
  }


}
