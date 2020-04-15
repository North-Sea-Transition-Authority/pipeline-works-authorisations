package uk.co.ogauthority.pwa.controller.pwaapplications.shared.huoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import javax.validation.Valid;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.HuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.AddHuooValidator;
import uk.co.ogauthority.pwa.validators.EditHuooValidator;

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
public class AddHuooController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final AddHuooValidator addHuooValidator;
  private final EditHuooValidator editHuooValidator;
  private final PadOrganisationRoleService padOrganisationRoleService;

  @Autowired
  public AddHuooController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      AddHuooValidator addHuooValidator,
      EditHuooValidator editHuooValidator,
      PadOrganisationRoleService padOrganisationRoleService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.addHuooValidator = addHuooValidator;
    this.editHuooValidator = editHuooValidator;
    this.padOrganisationRoleService = padOrganisationRoleService;
  }

  private ModelAndView getAddHuooModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/huoo/addHuoo")
        .addObject("huooRoles", HuooRole.stream()
            .sorted(Comparator.comparing(HuooRole::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, HuooRole::getDisplayText)))
        .addObject("treatyAgreements", TreatyAgreement.stream()
            .sorted(Comparator.comparing(TreatyAgreement::getCountry))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, TreatyAgreement::getCountry)))
        .addObject("huooTypes", HuooType.stream()
            .sorted(Comparator.comparing(HuooType::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, HuooType::getDisplayText)))
        .addObject("portalOrgs", portalOrganisationsAccessor.getAllOrganisationUnits()
            .stream()
            .sorted(Comparator.comparing(PortalOrganisationUnit::getName))
            .collect(
                StreamUtils.toLinkedHashMap(unit -> String.valueOf(unit.getOuId()), PortalOrganisationUnit::getName)));
    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView, "Add HUOO");
    return modelAndView;
  }

  @GetMapping("/add")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderAddHuoo(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") Integer applicationId,
                                    PwaApplicationContext applicationContext,
                                    @ModelAttribute("form") HuooForm form,
                                    AuthenticatedUserAccount user) {
    return getAddHuooModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping("/add")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postAddHuoo(@PathVariable("applicationType")
                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                  @PathVariable("applicationId") Integer applicationId,
                                  PwaApplicationContext applicationContext,
                                  @Valid @ModelAttribute("form") HuooForm form,
                                  BindingResult bindingResult,
                                  AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    addHuooValidator.validate(form, bindingResult, detail);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddHuooModelAndView(applicationContext.getApplicationDetail()), () -> {
          padOrganisationRoleService.createAndSaveEntityUsingForm(detail, form);
          return ReverseRouter.redirect(
              on(HuooController.class).renderHuooSummary(pwaApplicationType, detail.getMasterPwaApplicationId(), null, null));
        });
  }

  @GetMapping("/edit/{orgRoleId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderEditHuoo(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("orgRoleId") Integer orgRoleId,
                                     PwaApplicationContext applicationContext,
                                     @ModelAttribute("form") HuooForm form,
                                     AuthenticatedUserAccount user) {
    var padOrgRole = padOrganisationRoleService.getOrganisationRoleById(orgRoleId);
    padOrganisationRoleService.mapPadOrganisationRoleToForm(padOrgRole, form);
    return getAddHuooModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping("/edit/{orgRoleId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postEditHuoo(@PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   @PathVariable("applicationId") Integer applicationId,
                                   @PathVariable("orgRoleId") Integer orgRoleId,
                                   PwaApplicationContext applicationContext,
                                   @Valid @ModelAttribute("form") HuooForm form,
                                   BindingResult bindingResult,
                                   AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    var padOrgRole = padOrganisationRoleService.getOrganisationRoleById(orgRoleId);
    editHuooValidator.validate(form, bindingResult, detail, padOrgRole);
    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddHuooModelAndView(applicationContext.getApplicationDetail()), () -> {
          padOrganisationRoleService.saveEntityUsingForm(padOrgRole, form);
          return ReverseRouter.redirect(
              on(HuooController.class).renderHuooSummary(pwaApplicationType, detail.getMasterPwaApplicationId(), null, null));
        });
  }

  @PostMapping("/remove/{orgRoleId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postDeleteHuoo(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("orgRoleId") Integer orgRoleId,
                                     PwaApplicationContext applicationContext,
                                     @Valid @ModelAttribute("form") HuooForm form,
                                     BindingResult bindingResult,
                                     AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    var padOrgRole = padOrganisationRoleService.getOrganisationRoleById(orgRoleId);
    if (padOrganisationRoleService.canRemoveOrganisationRole(detail, padOrgRole)) {
      padOrganisationRoleService.removeRole(padOrgRole);
    }
    return ReverseRouter.redirect(on(HuooController.class)
        .renderHuooSummary(pwaApplicationType, detail.getMasterPwaApplicationId(), null, null));
  }

}
