package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipeline;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickSplitPipelineFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/split-pipeline/{huooRole}")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.HUOO_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
//@SessionAttributes("splitPipelineHuooJourneyData")
public class SplitPipelineHuooJourneyController {

  private static final String SELECT_PIPELINE_PAGE_HEADING_FORMAT = "Define pipeline split for %s";
  private static final String SELECT_PIPELINE_BACK_LINK_TEXT = "Back to " + StringUtils.uncapitalize(
      ApplicationTask.PIPELINES_HUOO.getShortenedDisplayName());

  private static final String SELECT_PIPELINE_HINT_FORMAT =
      "This will replace any existing splits and remove the %s defined for the selected pipeline";

  private final PadPipelinesHuooService padPipelinesHuooService;
  private final ControllerHelperService controllerHelperService;
  private final PickSplitPipelineFormValidator pickSplitPipelineFormValidator;

  @Autowired
  public SplitPipelineHuooJourneyController(PadPipelinesHuooService padPipelinesHuooService,
                                            ControllerHelperService controllerHelperService,
                                            PickSplitPipelineFormValidator pickSplitPipelineFormValidator) {
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.controllerHelperService = controllerHelperService;
    this.pickSplitPipelineFormValidator = pickSplitPipelineFormValidator;
  }

  @GetMapping("/select-pipeline")
  public ModelAndView renderSelectPipelineToSplit(@PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  @PathVariable("applicationId") int applicationId,
                                                  @PathVariable("huooRole") HuooRole huooRole,
                                                  PwaApplicationContext applicationContext,
                                                  @ModelAttribute("form") PickSplitPipelineForm form) {


    var modelAndView = getSelectPipelineModelAndView(applicationContext, huooRole);
    return modelAndView;
  }

  @PostMapping("/select-pipeline")
  public ModelAndView splitSelectedPipeline(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") int applicationId,
                                            @PathVariable("huooRole") HuooRole huooRole,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PickSplitPipelineForm form,
                                            BindingResult bindingResult,
                                            RedirectAttributes redirectAttributes) {

    pickSplitPipelineFormValidator.validate(form, bindingResult, applicationContext.getApplicationDetail(), huooRole);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getSelectPipelineModelAndView(applicationContext, huooRole),
        () -> {
          var selectedPipelineId = new PipelineId(form.getPipelineId());
          var selectedPipelineName = padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(
              applicationContext.getApplicationDetail(),
              selectedPipelineId
          ).getPipelineName();

          if (form.getNumberOfSections() == 1) {
            padPipelinesHuooService.removeSplitsForPipeline(
                applicationContext.getApplicationDetail(),
                selectedPipelineId,
                huooRole
            );
            FlashUtils.success(
                redirectAttributes,
                String.format("%s splits removed for pipeline %s", huooRole.getDisplayText(), selectedPipelineName)
            );
            return ReverseRouter.redirect(on(PipelinesHuooController.class)
                .renderSummary(pwaApplicationType, applicationId, null));
          }

          // TODO PWA-867 page 2 of journey
          FlashUtils.success(redirectAttributes,
              String.format("TODO: page of 2 of %s splits journey for %s", huooRole.getDisplayText(), selectedPipelineName)
          );
          return ReverseRouter.redirect(on(PipelinesHuooController.class)
              .renderSummary(pwaApplicationType, applicationId, null));

        });

  }

  private ModelAndView getSelectPipelineModelAndView(PwaApplicationContext applicationContext, HuooRole huooRole) {
    var huooRoleDisplayText = huooRole.getDisplayText().toLowerCase();

    var detail = applicationContext.getApplicationDetail();
    var options = padPipelinesHuooService.getSplitablePipelinesForAppAndMasterPwa(detail)
        .stream()
        .sorted(Comparator.comparing(NamedPipeline::getPipelineName))
        .collect(StreamUtils.toLinkedHashMap(
            namedPipeline -> String.valueOf(namedPipeline.getPipelineId()),
            NamedPipeline::getPipelineName
        ));

    return new ModelAndView("pwaApplication/shared/pipelinehuoo/splitPipelineHuooSelectPipeline")
        .addObject("backUrl", ReverseRouter.route(on(PipelinesHuooController.class).renderSummary(
            applicationContext.getApplicationType(), applicationContext.getMasterPwaApplicationId(), null
        )))
        .addObject("pipelineOptions", options)
        .addObject("pageHeading", String.format(SELECT_PIPELINE_PAGE_HEADING_FORMAT, huooRoleDisplayText))
        .addObject("backLinkText", SELECT_PIPELINE_BACK_LINK_TEXT)
        .addObject("selectPipelineHintText", String.format(SELECT_PIPELINE_HINT_FORMAT, huooRoleDisplayText));
  }

}
