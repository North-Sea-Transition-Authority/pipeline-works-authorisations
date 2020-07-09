package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineOption;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelineValidationType;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipeline-huoo/add/{huooRole}")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.HUOO_VARIATION
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@SessionAttributes("addPipelineHuooJourneyData")
public class AddPipelineHuooJourneyController {
  private static final String SELECT_PIPELINES_QUESTION_FORMAT = "On which pipelines do you want to assign %ss?";
  private static final String SELECT_PIPELINES_BACK_LINK_TEXT = "Back to " + StringUtils.uncapitalize(
      ApplicationTask.PIPELINES_HUOO.getShortenedDisplayName());
  public static final String UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT = "Back to pipeline selection";
  private static final String UPDATE_PIPELINE_ORG_ROLES_SUBMIT_BUTTON_FORMAT = "Update %ss for pipelines";
  private static final String UPDATE_PIPELINE_ORG_ROLES_QUESTION_FORMAT = " Who are the %ss for the selected pipelines?";


  @Bean
  @SessionScope
  public AddPipelineHuooJourneyData addPipelineHuooJourneyData() {
    return new AddPipelineHuooJourneyData();
  }

  @Resource(name = "addPipelineHuooJourneyData")
  private AddPipelineHuooJourneyData addPipelineHuooJourneyData;

  private final PadPipelinesHuooService padPipelinesHuooService;
  private final PickablePipelineService pickablePipelineService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public AddPipelineHuooJourneyController(
      PadPipelinesHuooService padPipelinesHuooService,
      PickablePipelineService pickablePipelineService,
      ControllerHelperService controllerHelperService) {
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.pickablePipelineService = pickablePipelineService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping("/pipelines")
  public ModelAndView renderPipelinesForHuooAssignment(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       @PathVariable("applicationId") int applicationId,
                                                       @PathVariable("huooRole") HuooRole huooRole,
                                                       PwaApplicationContext applicationContext,
                                                       @ModelAttribute("form") PickHuooPipelinesForm form) {

    addPipelineHuooJourneyData.updateFormWithPipelineJourneyData(
        applicationContext.getApplicationDetail(),
        huooRole,
        form);

    var modelAndView = getSelectPipelineModelAndView(applicationContext, huooRole);
    return modelAndView;
  }

  @PostMapping("/pipelines")
  public ModelAndView selectPipelinesForHuooAssignment(@PathVariable("applicationType")
                                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                       @PathVariable("applicationId") int applicationId,
                                                       @PathVariable("huooRole") HuooRole huooRole,
                                                       PwaApplicationContext applicationContext,
                                                       @ModelAttribute("form") PickHuooPipelinesForm form,
                                                       BindingResult bindingResult) {

    addPipelineHuooJourneyData.updateJourneyPipelineData(
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
            on(AddPipelineHuooJourneyController.class).renderOrganisationsForPipelineHuooAssignment(
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
    addPipelineHuooJourneyData.updateFormWithPipelineJourneyData(applicationDetail, huooRole, form);
    addPipelineHuooJourneyData.updateFormWithOrganisationRoleJourneyData(applicationDetail, huooRole, form);

    var modelAndView = getUpdatePipelineOrgRoleModelAndView(applicationContext, huooRole);


    return modelAndView;
  }

  @PostMapping(value = "/pipelines/organisations")
  public ModelAndView selectOrganisationsForPipelineHuooAssignment(@PathVariable("applicationType")
                                                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                                   @PathVariable("applicationId") int applicationId,
                                                                   @PathVariable("huooRole") HuooRole huooRole,
                                                                   PwaApplicationContext applicationContext,
                                                                   @ModelAttribute("form") PickHuooPipelinesForm form,
                                                                   BindingResult bindingResult,
                                                                   RedirectAttributes redirectAttributes) {
    var applicationDetail = applicationContext.getApplicationDetail();

    addPipelineHuooJourneyData.updateJourneyOrganisationData(
        applicationDetail,
        huooRole,
        form.getOrganisationUnitIds(),
        form.getTreatyAgreements()
    );
    addPipelineHuooJourneyData.updateFormWithPipelineJourneyData(applicationDetail, huooRole, form);

    padPipelinesHuooService.validateAddPipelineHuooForm(
        applicationContext.getApplicationDetail(),
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
          var pipelines = pickablePipelineService.getPickedPipelinesFromStrings(form.getPickedPipelineStrings());

          padPipelinesHuooService.updatePipelineHuooLinks(
              applicationContext.getApplicationDetail(),
              pipelines,
              huooRole,
              form.getOrganisationUnitIds()
                  .stream()
                  .map(OrganisationUnitId::new)
                  .collect(Collectors.toSet()),
              form.getTreatyAgreements());

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

    addPipelineHuooJourneyData.updateJourneyOrganisationData(
        applicationContext.getApplicationDetail(),
        huooRole,
        form.getOrganisationUnitIds(),
        form.getTreatyAgreements());

    return ReverseRouter.redirect(on(AddPipelineHuooJourneyController.class).renderPipelinesForHuooAssignment(
        pwaApplicationType,
        applicationId,
        huooRole,
        null,
        null
    ));
  }

  private ModelAndView getSelectPipelineModelAndView(PwaApplicationContext applicationContext, HuooRole huooRole) {
    var sortedPickablePipelineOptions = getSortedPickablePipelines(applicationContext.getApplicationDetail(), huooRole);

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinehuoo/addPipelineHuooSelectPipelines")
        .addObject("backUrl", ReverseRouter.route(on(PipelinesHuooController.class).renderSummary(
            applicationContext.getApplicationType(), applicationContext.getMasterPwaApplicationId(), null
        )))
        .addObject("pageHeading",
            String.format(SELECT_PIPELINES_QUESTION_FORMAT, huooRole.getDisplayText().toLowerCase()))
        .addObject("pickablePipelineOptions", sortedPickablePipelineOptions)
        .addObject("backLinkText", SELECT_PIPELINES_BACK_LINK_TEXT);
    return modelAndView;
  }

  private List<PickablePipelineOption> getSortedPickablePipelines(PwaApplicationDetail pwaApplicationDetail,
                                                                  HuooRole huooRole) {
    return padPipelinesHuooService.getSortedPickablePipelineOptionsForApplicationDetail(pwaApplicationDetail)
        .stream()
        .sorted(Comparator.comparing(PickablePipelineOption::getPipelineNumber))
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
        .addObject("pickablePipelineOptions", sortedPickablePipelineOptions)
        .addObject("backLinkText", SELECT_PIPELINES_BACK_LINK_TEXT)
        .addObject("pickableOrgDetails", orgUnitDetails)
        .addObject("availableTreatyOptions", availableTreatiesForRole);
  }
}
