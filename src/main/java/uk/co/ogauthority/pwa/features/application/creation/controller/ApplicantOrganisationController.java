package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.creation.ApplicantOrganisationFormValidator;
import uk.co.ogauthority.pwa.features.application.creation.ApplicantOrganisationService;
import uk.co.ogauthority.pwa.features.application.creation.PwaApplicationCreationService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.form.pwaapplications.ApplicantOrganisationForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa/{pwaId}/pwa-application/{applicationType}/applicant-organisation")
public class ApplicantOrganisationController {

  private final ApplicantOrganisationService applicantOrganisationService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationCreationService pwaApplicationCreationService;
  private final MasterPwaService masterPwaService;
  private final ApplicantOrganisationFormValidator applicantOrganisationFormValidator;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final ControllerHelperService controllerHelperService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public ApplicantOrganisationController(ApplicantOrganisationService applicantOrganisationService,
                                         PwaApplicationRedirectService pwaApplicationRedirectService,
                                         PwaApplicationCreationService pwaApplicationCreationService,
                                         MasterPwaService masterPwaService,
                                         ApplicantOrganisationFormValidator applicantOrganisationFormValidator,
                                         PortalOrganisationsAccessor portalOrganisationsAccessor,
                                         ControllerHelperService controllerHelperService,
                                         PwaHolderTeamService pwaHolderTeamService) {
    this.applicantOrganisationService = applicantOrganisationService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationCreationService = pwaApplicationCreationService;
    this.masterPwaService = masterPwaService;
    this.applicantOrganisationFormValidator = applicantOrganisationFormValidator;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.controllerHelperService = controllerHelperService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  private Map<String, String> getApplicantOrganisationOptionsOrThrow(MasterPwa masterPwa,
                                                                     WebUserAccount user) {

    var applicantOrganisationOptions = applicantOrganisationService.getPotentialApplicantOrganisations(masterPwa, user).stream()
        .sorted(Comparator.comparing(PortalOrganisationUnit::getName))
        .collect(StreamUtils.toLinkedHashMap(ou -> String.valueOf(ou.getOuId()), PortalOrganisationUnit::getName));

    if (applicantOrganisationOptions.isEmpty()) {
      throw new AccessDeniedException(String.format(
          "User with wua id [%s] can't choose an applicant organisation as they aren't in the holder team for any holders of PWA id [%s]",
          user.getWuaId(),
          masterPwa.getId()));
    }

    return applicantOrganisationOptions;

  }

  @GetMapping
  public ModelAndView renderSelectOrganisation(@PathVariable("pwaId") Integer pwaId,
                                               @PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @ModelAttribute("form") ApplicantOrganisationForm form,
                                               AuthenticatedUserAccount user) {

    ControllerUtils.startVariationControllerCheckAppType(pwaApplicationType);

    var masterPwa = masterPwaService.getMasterPwaById(pwaId);

    var applicantOrganisationOptions = getApplicantOrganisationOptionsOrThrow(masterPwa, user);

    return getModelAndView(pwaApplicationType, masterPwa, applicantOrganisationOptions, user);

  }

  private ModelAndView getModelAndView(PwaApplicationType pwaApplicationType,
                                       MasterPwa masterPwa,
                                       Map<String, String> applicantOrganisationOptions,
                                       WebUserAccount user) {

    String backUrl = ReverseRouter.route(on(PickExistingPwaController.class)
        .renderPickPwaToStartApplication(pwaApplicationType, PwaResourceType.PETROLEUM, null, null));

    var pwaDetail = masterPwaService.getCurrentDetailOrThrow(masterPwa);

    String userOrgGroupsCsv = pwaHolderTeamService
        .getPortalOrganisationGroupsWhereUserHasOrgRole(user, PwaOrganisationRole.APPLICATION_CREATOR)
        .stream()
        .sorted(Comparator.comparing(PortalOrganisationGroup::getName))
        .map(PortalOrganisationGroup::getName)
        .collect(Collectors.joining(", "));

    return new ModelAndView("pwaApplication/shared/pickApplicantOrganisation")
        .addObject("backUrl", backUrl)
        .addObject("applicantOrganisationOptions", applicantOrganisationOptions)
        .addObject("pwaReference", pwaDetail.getReference())
        .addObject("userOrgGroupsCsv", userOrgGroupsCsv);

  }

  @PostMapping
  public ModelAndView selectOrganisation(@PathVariable("pwaId") Integer pwaId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @ModelAttribute("form") ApplicantOrganisationForm form,
                                         BindingResult bindingResult,
                                         AuthenticatedUserAccount user) {

    ControllerUtils.startVariationControllerCheckAppType(pwaApplicationType);

    var masterPwa = masterPwaService.getMasterPwaById(pwaId);

    var applicantOrganisationOptions =  getApplicantOrganisationOptionsOrThrow(masterPwa, user);

    applicantOrganisationFormValidator.validate(form, bindingResult, masterPwa, user);

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getModelAndView(pwaApplicationType, masterPwa, applicantOrganisationOptions, user),
        () -> {

          var applicantOrganisation = portalOrganisationsAccessor
              .getOrganisationUnitById(form.getApplicantOrganisationOuId())
              .orElseThrow(() -> new RuntimeException(
                  String.format("Couldn't find an organisation unit with id [%s]", form.getApplicantOrganisationOuId())));

          var newAppDetail = pwaApplicationCreationService
              .createVariationPwaApplication(masterPwa, pwaApplicationType, PwaResourceType.PETROLEUM, applicantOrganisation, user);

          return pwaApplicationRedirectService.getTaskListRedirect(newAppDetail.getPwaApplication());

        });

  }

}
