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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.huoo.AddHuooController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.generic.SummaryForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/huoo")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.HUOO_VARIATION
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class HuooController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public HuooController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PadOrganisationRoleService padOrganisationRoleService,
      PwaApplicationRedirectService pwaApplicationRedirectService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getHuooModelAndView(PwaApplicationDetail pwaApplicationDetail) {

    var padOrganisationRoleList = padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail);

    var modelAndView = new ModelAndView("pwaApplication/shared/huoo/overview")
        .addObject("addHuooUrl", ReverseRouter.route(on(AddHuooController.class)
            .renderAddHuoo(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null, null)))
        .addObject("huooOrgs", padOrganisationRoleService
            .getHuooOrganisationUnitRoleViews(pwaApplicationDetail, padOrganisationRoleList))
        .addObject("treatyAgreements",
            padOrganisationRoleService.getTreatyAgreementViews(pwaApplicationDetail, padOrganisationRoleList))
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()));

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
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") SummaryForm form,
                                      BindingResult bindingResult,
                                      AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    bindingResult = padOrganisationRoleService.validate(form, bindingResult, ValidationType.FULL, detail);
    if (bindingResult.hasErrors()) {
      return getHuooModelAndView(detail)
          .addObject("errorMessage", "You must have at least one holder, user, operator, and owner");
    }
    return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
  }


}
