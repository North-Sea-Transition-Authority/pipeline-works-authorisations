package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.ModifyPipelineHuooJourneyData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickHuooPipelineValidationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineOption;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.ReconciledHuooPickablePipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.controller.PipelinesHuooController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipeline-huoo/{huooRole}")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.HUOO_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@SessionAttributes("modifyPipelineHuooJourneyData")
public class ModifyPipelineHuooJourneyController {
  private static final String SELECT_PIPELINES_QUESTION_FORMAT = "On which pipelines do you want to assign %ss?";
  private static final String SELECT_PIPELINES_BACK_LINK_TEXT = "Back to " + StringUtils.uncapitalize(
      ApplicationTask.PIPELINES_HUOO.getShortenedDisplayName());
  public static final String UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT = "Back to pipeline selection";
  private static final String UPDATE_PIPELINE_ORG_ROLES_SUBMIT_BUTTON_FORMAT = "Update %ss for pipelines";
  private static final String UPDATE_PIPELINE_ORG_ROLES_QUESTION_FORMAT = " Who are the %ss for the selected pipelines?";


  @Bean
  @SessionScope
  public ModifyPipelineHuooJourneyData modifyPipelineHuooJourneyData() {
    return new ModifyPipelineHuooJourneyData();
  }

  @Resource(name = "modifyPipelineHuooJourneyData")
  private ModifyPipelineHuooJourneyData modifyPipelineHuooJourneyData;

  private final PadPipelinesHuooService padPipelinesHuooService;
  private final PickableHuooPipelineService pickableHuooPipelineService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public ModifyPipelineHuooJourneyController(
      PadPipelinesHuooService padPipelinesHuooService,
      PickableHuooPipelineService pickableHuooPipelineService,
      ControllerHelperService controllerHelperService) {
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.pickableHuooPipelineService = pickableHuooPipelineService;
    this.controllerHelperService = controllerHelperService;
  }

  @PostMapping("/editGroup")
  public ModelAndView editGroupRouter(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") int applicationId,
                                      @PathVariable("huooRole") HuooRole huooRole,
                                      PwaApplicationContext applicationContext,
                                      @RequestParam(value = "journeyPage") JourneyPage journeyPage,
                                      @RequestParam(value = "encodedPickedPipelineIds", required = false)
                                          Set<String> encodedPickedPipelineIds,
                                      @RequestParam(value = "organisationIds", required = false) Set<Integer> organisationUnitIds,
                                      @RequestParam(value = "treatyAgreements", required = false) Set<TreatyAgreement> treatyAgreements) {

    var detail = applicationContext.getApplicationDetail();
    modifyPipelineHuooJourneyData.reset();

    // Workaround using Base64 encoded strings as params
    // Otherwise url params in the desired format are not being correctly mapped as single string entries into the set.
    var decodedPickedPipelineIds = decodeEncodedStrings(SetUtils.emptyIfNull(encodedPickedPipelineIds));

    var reconciledPipelinePickableIds = padPipelinesHuooService.reconcilePickablePipelinesFromPipelineIds(
        applicationContext.getApplicationDetail(),
        huooRole,
        decodedPickedPipelineIds
    )
        .stream()
        .map(ReconciledHuooPickablePipeline::getPickableIdAsString)
        .collect(Collectors.toSet());

    // update journey pipelines
    modifyPipelineHuooJourneyData.updateJourneyPipelineData(detail, huooRole, reconciledPipelinePickableIds);

    var reconciledRoleOwners = padPipelinesHuooService.reconcileOrganisationRoleOwnersFrom(
        detail,
        huooRole,
        SetUtils.emptyIfNull(organisationUnitIds),
        SetUtils.emptyIfNull(treatyAgreements)
    );

    var reconciledOrgUnitIds = reconciledRoleOwners.stream()
        .filter(o -> HuooType.PORTAL_ORG.equals(o.getHuooType()))
        .map(o -> o.getOrganisationUnitId().asInt())
        .collect(Collectors.toSet());

    var reconciledTreaties = reconciledRoleOwners.stream()
        .filter(o -> HuooType.TREATY_AGREEMENT.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getTreatyAgreement)
        .collect(Collectors.toSet());

    //update journey orgs role owners
    modifyPipelineHuooJourneyData.updateJourneyOrganisationData(detail, huooRole, reconciledOrgUnitIds,
        reconciledTreaties);

    return redirectToJourneyPage(applicationContext, huooRole, journeyPage);
  }

  private Set<String> decodeEncodedStrings(Set<String> encodedStrings) {
    var decoder = Base64.getUrlDecoder();
    return encodedStrings.stream()
        // yuck
        .map(s -> new String(decoder.decode(s)))
        .collect(Collectors.toSet());
  }

  @GetMapping("/pipelines")
  public ModelAndView renderPipelinesForHuooAssignment(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       @PathVariable("applicationId") int applicationId,
                                                       @PathVariable("huooRole") HuooRole huooRole,
                                                       PwaApplicationContext applicationContext,
                                                       @ModelAttribute("form") PickHuooPipelinesForm form) {

    modifyPipelineHuooJourneyData.updateFormWithPipelineJourneyData(
        applicationContext.getApplicationDetail(),
        huooRole,
        form);

    return getSelectPipelineModelAndView(applicationContext, huooRole);
  }

  @PostMapping("/pipelines")
  public ModelAndView selectPipelinesForHuooAssignment(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       @PathVariable("applicationId") int applicationId,
                                                       @PathVariable("huooRole") HuooRole huooRole,
                                                       PwaApplicationContext applicationContext,
                                                       @ModelAttribute("form") PickHuooPipelinesForm form,
                                                       BindingResult bindingResult) {

    modifyPipelineHuooJourneyData.updateJourneyPipelineData(
        applicationContext.getApplicationDetail(),
        huooRole,
        form.getPickedPipelineStrings());

    padPipelinesHuooService.validateAddPipelineHuooForm(
        applicationContext.getApplicationDetail(),
        form,
        bindingResult,
        PickHuooPipelineValidationType.PIPELINES,
        huooRole
    );

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getSelectPipelineModelAndView(applicationContext, huooRole),
        () -> ReverseRouter.redirect(
            on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
                pwaApplicationType,
                applicationId,
                huooRole,
                null,
                null
            )));
  }

  @GetMapping("/pipelines/organisations")
  public ModelAndView renderOrganisationsForPipelineHuooAssignment(@PathVariable("applicationType")
                                                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                                   @PathVariable("applicationId") int applicationId,
                                                                   @PathVariable("huooRole") HuooRole huooRole,
                                                                   PwaApplicationContext applicationContext,
                                                                   @ModelAttribute("form") PickHuooPipelinesForm form) {
    var applicationDetail = applicationContext.getApplicationDetail();
    modifyPipelineHuooJourneyData.updateFormWithPipelineJourneyData(applicationDetail, huooRole, form);
    modifyPipelineHuooJourneyData.updateFormWithOrganisationRoleJourneyData(applicationDetail, huooRole, form);

    return getUpdatePipelineOrgRoleModelAndView(applicationContext, huooRole);
  }

  @PostMapping(value = "/pipelines/organisations")
  public ModelAndView selectOrganisationsForPipelineHuooAssignment(@PathVariable("applicationType")
                                                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                                   @PathVariable("applicationId") int applicationId,
                                                                   @PathVariable("huooRole") HuooRole huooRole,
                                                                   PwaApplicationContext applicationContext,
                                                                   @ModelAttribute("form") PickHuooPipelinesForm form,
                                                                   BindingResult bindingResult,
                                                                   RedirectAttributes redirectAttributes,
                                                                   SessionStatus sessionStatus) {
    var applicationDetail = applicationContext.getApplicationDetail();

    modifyPipelineHuooJourneyData.updateJourneyOrganisationData(
        applicationDetail,
        huooRole,
        form.getOrganisationUnitIds(),
        form.getTreatyAgreements()
    );
    modifyPipelineHuooJourneyData.updateFormWithPipelineJourneyData(applicationDetail, huooRole, form);

    padPipelinesHuooService.validateAddPipelineHuooForm(
        applicationDetail,
        form,
        bindingResult,
        PickHuooPipelineValidationType.FULL,
        huooRole
    );

    var modelAndView = getUpdatePipelineOrgRoleModelAndView(applicationContext, huooRole);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        modelAndView,
        () -> {
          // This is not direct form -> entity mapping so diverges from project standard imo.
          // actual mapping between form elements and entities is still captured in the service code.
          var pipelineIdentifiers = pickableHuooPipelineService.getPickedPipelinesFromStrings(
              applicationDetail,
              huooRole,
              form.getPickedPipelineStrings());

          padPipelinesHuooService.updatePipelineHuooLinks(
              applicationContext.getApplicationDetail(),
              pipelineIdentifiers,
              huooRole,
              form.getOrganisationUnitIds()
                  .stream()
                  .map(OrganisationUnitId::new)
                  .collect(Collectors.toSet()),
              form.getTreatyAgreements());

          // make sure we clear journey data on completion.
          sessionStatus.setComplete();
          modifyPipelineHuooJourneyData.reset();

          FlashUtils.success(
              redirectAttributes,
              String.format("Pipeline %ss assigned", huooRole.getDisplayText().toLowerCase()));

          return ReverseRouter.redirect(on(PipelinesHuooController.class).renderSummary(
              pwaApplicationType, applicationId, null
          ));
        }
    );

  }

  @PostMapping(value = "/pipelines/organisations", params = UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT)
  public ModelAndView returnToPipelineSelection(@PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                @PathVariable("applicationId") int applicationId,
                                                @PathVariable("huooRole") HuooRole huooRole,
                                                PwaApplicationContext applicationContext,
                                                @ModelAttribute("form") PickHuooPipelinesForm form) {

    modifyPipelineHuooJourneyData.updateJourneyOrganisationData(
        applicationContext.getApplicationDetail(),
        huooRole,
        form.getOrganisationUnitIds(),
        form.getTreatyAgreements());

    return ReverseRouter.redirect(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
        pwaApplicationType,
        applicationId,
        huooRole,
        null,
        null
    ));
  }

  private ModelAndView redirectToJourneyPage(PwaApplicationContext applicationContext, HuooRole huooRole,
                                             JourneyPage journeyPage) {
    if (JourneyPage.ORGANISATION_SELECTION.equals(journeyPage)) {
      return ReverseRouter.redirect(
          on(ModifyPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
              applicationContext.getApplicationType(),
              applicationContext.getMasterPwaApplicationId(),
              huooRole,
              null,
              null
          ));
    }

    return ReverseRouter.redirect(on(ModifyPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
        applicationContext.getApplicationType(),
        applicationContext.getMasterPwaApplicationId(),
        huooRole,
        null,
        null
    ));

  }

  private ModelAndView getSelectPipelineModelAndView(PwaApplicationContext applicationContext, HuooRole huooRole) {
    var sortedPickablePipelineOptions = getSortedPickablePipelines(applicationContext.getApplicationDetail(), huooRole);

    return new ModelAndView("pwaApplication/shared/pipelinehuoo/addPipelineHuooSelectPipelines")
        .addObject("backUrl", ReverseRouter.route(on(PipelinesHuooController.class).renderSummary(
            applicationContext.getApplicationType(), applicationContext.getMasterPwaApplicationId(), null
        )))
        .addObject("pageHeading",
            String.format(SELECT_PIPELINES_QUESTION_FORMAT, huooRole.getDisplayText().toLowerCase()))
        .addObject("pickableHuooPipelineOptions", sortedPickablePipelineOptions)
        .addObject("backLinkText", SELECT_PIPELINES_BACK_LINK_TEXT);
  }

  private List<PickableHuooPipelineOption> getSortedPickablePipelines(PwaApplicationDetail pwaApplicationDetail,
                                                                      HuooRole huooRole) {
    return padPipelinesHuooService.getSortedPickablePipelineOptionsForApplicationDetail(pwaApplicationDetail, huooRole)
        .stream()
        .sorted(Comparator.comparing(PickableHuooPipelineOption::getPipelineNumber))
        .collect(Collectors.toList());
  }

  private ModelAndView getUpdatePipelineOrgRoleModelAndView(PwaApplicationContext applicationContext,
                                                            HuooRole huooRole) {
    var sortedPickablePipelineOptions = getSortedPickablePipelines(applicationContext.getApplicationDetail(), huooRole);

    var orgUnitDetails = padPipelinesHuooService.getAvailableOrgUnitDetailsForRole(
        applicationContext.getApplicationDetail(), huooRole);

    var availableTreatiesForRole = padPipelinesHuooService.getAvailableTreatyAgreementsForRole(
        applicationContext.getApplicationDetail(),
        huooRole
    ).stream()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, TreatyAgreement::getAgreementText));

    return new ModelAndView("pwaApplication/shared/pipelinehuoo/addPipelineHuooAssociateOrganisations")
        .addObject("pageHeading",
            String.format(UPDATE_PIPELINE_ORG_ROLES_QUESTION_FORMAT, huooRole.getDisplayText().toLowerCase()))
        .addObject("submitButtonText",
            String.format(UPDATE_PIPELINE_ORG_ROLES_SUBMIT_BUTTON_FORMAT, huooRole.getDisplayText().toLowerCase()))
        .addObject("pickableHuooPipelineOptions", sortedPickablePipelineOptions)
        .addObject("backLinkText", SELECT_PIPELINES_BACK_LINK_TEXT)
        .addObject("pickableOrgDetails", orgUnitDetails)
        .addObject("availableTreatyOptions", availableTreatiesForRole);
  }

  public enum JourneyPage {
    PIPELINE_SELECTION, ORGANISATION_SELECTION
  }
}
