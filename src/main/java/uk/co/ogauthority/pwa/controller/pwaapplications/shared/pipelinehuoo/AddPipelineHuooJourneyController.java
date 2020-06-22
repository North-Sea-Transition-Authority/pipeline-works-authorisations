package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
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
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineOption;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickablePipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PipelinesHuooService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
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
  private static final String SELECT_PIPELINES_BACK_LINK_TEXT = "Back to " + ApplicationTask.PIPELINES_HUOO.getDisplayName().toLowerCase();

  public static final String UPDATE_PIPELINE_ORG_ROLES_BACK_BUTTON_TEXT = "Back to pipeline selection";
  private static final String UPDATE_PIPELINE_ORG_ROLES_SUBMIT_BUTTON_FORMAT = "Update %ss for pipelines";
  private static final String UPDATE_PIPELINE_ORG_ROLES_QUESTION_FORMAT = " Who are the %ss for the selected pipelines";


  @Bean
  @SessionScope
  public AddPipelineHuooJourneyData addPipelineHuooJourneyData() {
    return new AddPipelineHuooJourneyData();
  }

  @Resource(name = "addPipelineHuooJourneyData")
  private AddPipelineHuooJourneyData addPipelineHuooJourneyData;

  private final PipelinesHuooService pipelinesHuooService;
  private final PickablePipelineService pickablePipelineService;

  @Autowired
  public AddPipelineHuooJourneyController(
      PipelinesHuooService pipelinesHuooService,
      PickablePipelineService pickablePipelineService) {
    this.pipelinesHuooService = pipelinesHuooService;

    this.pickablePipelineService = pickablePipelineService;
  }

  @GetMapping("/pipelines")
  public ModelAndView renderAddPipelineHuoo(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") int applicationId,
                                            @PathVariable("huooRole") HuooRole huooRole,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PickHuooPipelinesForm form) {

    addPipelineHuooJourneyData.updateFormWithPipelineJourneyData(huooRole, form);

    var modelAndView = getSelectPipelineModelAndView(applicationContext, huooRole);
    return modelAndView;
  }

  @PostMapping("/pipelines")
  public ModelAndView addPipelineHuoo(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") int applicationId,
                                      @PathVariable("huooRole") HuooRole huooRole,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") PickHuooPipelinesForm form,
                                      BindingResult bindingResult) {

    addPipelineHuooJourneyData.updateJourneyPipelineData(huooRole, form.getPickedPipelineStrings());

    pipelinesHuooService.validateAddPipelineHuooForm(
        applicationContext.getApplicationDetail(),
        form,
        bindingResult,
        PickHuooPipelineValidationType.PIPELINES,
        huooRole
    );

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getSelectPipelineModelAndView(applicationContext, huooRole),
        () -> ReverseRouter.redirect(on(AddPipelineHuooJourneyController.class).renderAddPipelineHuooOrganisations(
            pwaApplicationType,
            applicationId,
            huooRole,
            null,
            null
        )));


  }

  @GetMapping("/pipelines/organisations")
  public ModelAndView renderAddPipelineHuooOrganisations(@PathVariable("applicationType")
                                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                         @PathVariable("applicationId") int applicationId,
                                                         @PathVariable("huooRole") HuooRole huooRole,
                                                         PwaApplicationContext applicationContext,
                                                         @ModelAttribute("form") PickHuooPipelinesForm form) {
    addPipelineHuooJourneyData.updateFormWithPipelineJourneyData(huooRole, form);
    addPipelineHuooJourneyData.updateFormWithOrganisationRoleJourneyData(huooRole, form);

    var modelAndView = getUpdatePipelineOrgRoleModelAndView(applicationContext, huooRole);


    return modelAndView;
  }

  @PostMapping(value = "/pipelines/organisations")
  public ModelAndView updatePipelineOrganisationRoles(@PathVariable("applicationType")
                                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                      @PathVariable("applicationId") int applicationId,
                                                      @PathVariable("huooRole") HuooRole huooRole,
                                                      PwaApplicationContext applicationContext,
                                                      @ModelAttribute("form") PickHuooPipelinesForm form,
                                                      BindingResult bindingResult,
                                                      RedirectAttributes redirectAttributes) {

    addPipelineHuooJourneyData.updateJourneyOrganisationData(huooRole, form.getOrganisationUnitIds());
    addPipelineHuooJourneyData.updateFormWithPipelineJourneyData(huooRole, form);

    pipelinesHuooService.validateAddPipelineHuooForm(
        applicationContext.getApplicationDetail(),
        form,
        bindingResult,
        PickHuooPipelineValidationType.FULL,
        huooRole
    );

    var modelAndView = getUpdatePipelineOrgRoleModelAndView(applicationContext, huooRole);

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        modelAndView,
        () -> {
          // This is not direct form -> entity mapping so diverges from project standard imo.
          // actual mapping between form elements and entities is still captured in the service code.
          var pipelines = pickablePipelineService.getPickedPipelinesFromStrings(form.getPickedPipelineStrings());
          var organisationRoles = pipelinesHuooService.getPadOrganisationRolesFrom(
              applicationContext.getApplicationDetail(),
              huooRole,
              form.getOrganisationUnitIds());

          pipelinesHuooService.createPipelineOrganisationRoles(
              applicationContext.getApplicationDetail(),
              organisationRoles,
              pipelines);

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

    addPipelineHuooJourneyData.updateJourneyOrganisationData(huooRole, form.getOrganisationUnitIds());

    return ReverseRouter.redirect(on(AddPipelineHuooJourneyController.class).renderAddPipelineHuoo(
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
    return pipelinesHuooService.getPickablePipelineOptionsWithNoRoleOfType(pwaApplicationDetail, huooRole)
        .stream()
        .sorted(Comparator.comparing(PickablePipelineOption::getPipelineNumber))
        .collect(Collectors.toList());

  }

  private ModelAndView getUpdatePipelineOrgRoleModelAndView(PwaApplicationContext applicationContext,
                                                            HuooRole huooRole) {
    var sortedPickablePipelineOptions = getSortedPickablePipelines(applicationContext.getApplicationDetail(), huooRole);

    var orgUnitDetails = pipelinesHuooService.getAvailableOrgUnitDetailsForRole(
        applicationContext.getApplicationDetail(), huooRole);

    var modelAndView = new ModelAndView("pwaApplication/shared/pipelinehuoo/addPipelineHuooAssociateOrganisations")
        .addObject("pageHeading",
            String.format(UPDATE_PIPELINE_ORG_ROLES_QUESTION_FORMAT, huooRole.getDisplayText().toLowerCase()))
        .addObject("submitButtonText",
            String.format(UPDATE_PIPELINE_ORG_ROLES_SUBMIT_BUTTON_FORMAT, huooRole.getDisplayText().toLowerCase()))
        .addObject("pickablePipelineOptions", sortedPickablePipelineOptions)
        .addObject("backLinkText", SELECT_PIPELINES_BACK_LINK_TEXT)
        .addObject("pickableOrgDetails", orgUnitDetails);
    return modelAndView;
  }
}
