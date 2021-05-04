package uk.co.ogauthority.pwa.controller.pwaapplications.shared.huoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import javax.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.HuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.huoo.AddHuooValidator;
import uk.co.ogauthority.pwa.validators.huoo.EditHuooValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/huoo")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.HUOO_VARIATION
})
public class AddHuooController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final AddHuooValidator addHuooValidator;
  private final EditHuooValidator editHuooValidator;
  private final PadOrganisationRoleService padOrganisationRoleService;
  private final ControllerHelperService controllerHelperService;
  private final String ogaServiceDeskEmail;

  @Autowired
  public AddHuooController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      AddHuooValidator addHuooValidator,
      EditHuooValidator editHuooValidator,
      PadOrganisationRoleService padOrganisationRoleService,
      ControllerHelperService controllerHelperService,
      @Value("${oga.servicedesk.email}") String ogaServiceDeskEmail) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.addHuooValidator = addHuooValidator;
    this.editHuooValidator = editHuooValidator;
    this.padOrganisationRoleService = padOrganisationRoleService;
    this.controllerHelperService = controllerHelperService;
    this.ogaServiceDeskEmail = ogaServiceDeskEmail;
  }

  private void addObjectAttributes(PwaApplicationDetail detail, ModelAndView modelAndView) {
    modelAndView.addObject("huooRoles", HuooRole.stream()
        .sorted(Comparator.comparing(HuooRole::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, HuooRole::getDisplayText)))
        .addObject("treatyAgreements", TreatyAgreement.stream()
            .sorted(Comparator.comparing(TreatyAgreement::getCountry))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, TreatyAgreement::getCountry)))
        .addObject("huooTypes", HuooType.streamSelectable()
            .sorted(Comparator.comparing(HuooType::getDisplayOrder))
            .collect(StreamUtils.toLinkedHashMap(Enum::name, HuooType::getDisplayText)))
        .addObject("portalOrgs", portalOrganisationsAccessor.getAllOrganisationUnits()
            .stream()
            .sorted(Comparator.comparing(PortalOrganisationUnit::getName))
            .collect(
                StreamUtils.toLinkedHashMap(unit -> String.valueOf(unit.getOuId()), PortalOrganisationUnit::getName)))
        .addObject("backUrl", ReverseRouter.route(on(HuooController.class)
            .renderHuooSummary(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));
  }

  private ModelAndView getAddHuooModelAndView(PwaApplicationDetail pwaApplicationDetail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/huoo/addHuoo")
        .addObject("screenActionType", ScreenActionType.ADD)
        .addObject("ogaServiceDeskEmail", ogaServiceDeskEmail);
    addObjectAttributes(pwaApplicationDetail, modelAndView);
    applicationBreadcrumbService.fromHuoo(pwaApplicationDetail.getPwaApplication(), modelAndView, "Add HUOO");
    return modelAndView;
  }

  private ModelAndView getEditHuooModelAndView(PwaApplicationDetail pwaApplicationDetail, HuooType huooType) {
    var modelAndView = new ModelAndView("pwaApplication/shared/huoo/editHuoo")
        .addObject("screenActionType", ScreenActionType.EDIT)
        .addObject("huooType", huooType);
    addObjectAttributes(pwaApplicationDetail, modelAndView);
    applicationBreadcrumbService.fromHuoo(pwaApplicationDetail.getPwaApplication(), modelAndView, "Edit HUOO");
    return modelAndView;
  }

  @GetMapping("/add")
  public ModelAndView renderAddHuoo(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") Integer applicationId,
                                    PwaApplicationContext applicationContext,
                                    @ModelAttribute("form") HuooForm form,
                                    AuthenticatedUserAccount user) {
    return getAddHuooModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping("/add")
  public ModelAndView postAddHuoo(@PathVariable("applicationType")
                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                  @PathVariable("applicationId") Integer applicationId,
                                  PwaApplicationContext applicationContext,
                                  @Valid @ModelAttribute("form") HuooForm form,
                                  BindingResult bindingResult,
                                  AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    addHuooValidator.validate(form, bindingResult, detail);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddHuooModelAndView(detail), () -> {
          padOrganisationRoleService.saveEntityUsingForm(detail, form);
          return ReverseRouter.redirect(
              on(HuooController.class).renderHuooSummary(pwaApplicationType, detail.getMasterPwaApplicationId(), null,
                  null));
        });
  }

  @GetMapping("/edit/org/{orgUnitId}")
  public ModelAndView renderEditOrgHuoo(@PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("orgUnitId") Integer orgUnitId,
                                        PwaApplicationContext applicationContext,
                                        @ModelAttribute("form") HuooForm form,
                                        AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    var orgUnit = portalOrganisationsAccessor.getOrganisationUnitById(orgUnitId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find organisation unit with ID: " + orgUnitId));
    padOrganisationRoleService.mapPortalOrgUnitRoleToForm(detail, orgUnit, form);
    return getEditHuooModelAndView(applicationContext.getApplicationDetail(), HuooType.PORTAL_ORG)
        .addObject("huooType", HuooType.PORTAL_ORG);
  }

  @PostMapping("/edit/org/{orgUnitId}")
  public ModelAndView postEditOrgHuoo(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("orgUnitId") Integer orgUnitId,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") HuooForm form,
                                      BindingResult bindingResult,
                                      AuthenticatedUserAccount user) {
    form.setHuooType(HuooType.PORTAL_ORG);
    var detail = applicationContext.getApplicationDetail();
    var orgUnit = portalOrganisationsAccessor.getOrganisationUnitById(orgUnitId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find organisation unit with ID: " + orgUnitId));
    editHuooValidator.validate(form, bindingResult, detail,
        padOrganisationRoleService.getValidationViewForOrg(detail, orgUnit), user);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getEditHuooModelAndView(detail, HuooType.PORTAL_ORG), () -> {
          padOrganisationRoleService.updateOrgRolesUsingForm(detail, form, orgUnit);
          return ReverseRouter.redirect(
              on(HuooController.class).renderHuooSummary(pwaApplicationType, detail.getMasterPwaApplicationId(), null,
                  null));
        });
  }

  @GetMapping("/remove/org/{orgUnitId}")
  public ModelAndView renderRemoveOrgHuoo(@PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("orgUnitId") Integer orgUnitId,
                                        PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    var orgUnit = portalOrganisationsAccessor.getOrganisationUnitById(orgUnitId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find organisation unit with ID: " + orgUnitId));
    return getRemoveHuooModelAndView(detail)
        .addObject("huooName", orgUnit.getName())
        .addObject("backUrl", ReverseRouter.route(on(HuooController.class)
            .renderHuooSummary(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));
  }

  @PostMapping("/remove/org/{orgUnitId}")
  public ModelAndView postRemoveOrgHuoo(@PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("orgUnitId") Integer orgUnitId,
                                      PwaApplicationContext applicationContext,
                                      RedirectAttributes redirectAttributes) {
    var detail = applicationContext.getApplicationDetail();
    var orgUnit = portalOrganisationsAccessor.getOrganisationUnitById(orgUnitId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find organisation unit with ID: " + orgUnitId));
    if (padOrganisationRoleService.canRemoveOrgRoleFromUnit(detail, orgUnit)) {
      padOrganisationRoleService.removeRolesOfUnit(detail, orgUnit);
    }
    FlashUtils.info(
        redirectAttributes,
        String.format("Removed legal entity %s", orgUnit.getName()));
    return ReverseRouter.redirect(on(HuooController.class)
        .renderHuooSummary(pwaApplicationType, detail.getMasterPwaApplicationId(), null, null));
  }

  @GetMapping("/remove/treaty-agreement/{orgRoleId}")
  public ModelAndView renderRemoveTreatyHuoo(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("orgRoleId") Integer orgRoleId,
                                             PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    return getRemoveHuooModelAndView(detail)
        .addObject("huooName", "Treaty agreement")
        .addObject("backUrl", ReverseRouter.route(on(HuooController.class)
            .renderHuooSummary(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));
  }

  @PostMapping("/remove/treaty-agreement/{orgRoleId}")
  public ModelAndView postRemoveTreatyHuoo(@PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        @PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("orgRoleId") Integer orgRoleId,
                                        PwaApplicationContext applicationContext,
                                        RedirectAttributes redirectAttributes) {
    var detail = applicationContext.getApplicationDetail();
    var orgRole = padOrganisationRoleService.getOrganisationRole(detail, orgRoleId);
    padOrganisationRoleService.removeRoleOfTreatyAgreement(orgRole);
    FlashUtils.info(
        redirectAttributes,
        String.format("Removed %s treaty agreement", orgRole.getAgreement().getCountry()));
    return ReverseRouter.redirect(on(HuooController.class)
        .renderHuooSummary(pwaApplicationType, detail.getMasterPwaApplicationId(), null, null));
  }

  private ModelAndView getRemoveHuooModelAndView(PwaApplicationDetail detail) {
    return new ModelAndView("pwaApplication/shared/pipelinehuoo/removeHuoo")
        .addObject("backUrl", ReverseRouter.route(on(HuooController.class)
            .renderHuooSummary(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null)));
  }

}
