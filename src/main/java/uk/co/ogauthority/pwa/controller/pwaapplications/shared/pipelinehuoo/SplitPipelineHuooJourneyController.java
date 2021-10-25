package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form.DefinePipelineHuooSectionsForm;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.DefinePipelineHuooSectionValidationHint;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.DefinePipelineHuooSectionsFormValidator;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickSplitPipelineFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/split/{huooRole}")
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
public class SplitPipelineHuooJourneyController {

  private static final String SELECT_PIPELINE_PAGE_HEADING_FORMAT = "Define pipeline split for %s";
  private static final String SELECT_PIPELINE_BACK_LINK_TEXT = "Back to " + StringUtils.uncapitalize(
      ApplicationTask.PIPELINES_HUOO.getShortenedDisplayName());

  private static final String DEFINE_SECTIONS_PAGE_HEADING_FORMAT = "Define sections for %s %ss";
  private static final String DEFINE_SECTIONS_BACK_LINK_TEXT = "Back to select pipeline";

  private static final String SELECT_PIPELINE_HINT_FORMAT =
      "This will replace any existing splits and remove the %ss defined for the selected pipeline";

  private final PadPipelinesHuooService padPipelinesHuooService;
  private final ControllerHelperService controllerHelperService;
  private final PickSplitPipelineFormValidator pickSplitPipelineFormValidator;
  private final PickableHuooPipelineIdentService pickableHuooPipelineIdentService;
  private final DefinePipelineHuooSectionsFormValidator definePipelineHuooSectionsFormValidator;


  @Autowired
  public SplitPipelineHuooJourneyController(PadPipelinesHuooService padPipelinesHuooService,
                                            ControllerHelperService controllerHelperService,
                                            PickSplitPipelineFormValidator pickSplitPipelineFormValidator,
                                            PickableHuooPipelineIdentService pickableHuooPipelineIdentService,
                                            DefinePipelineHuooSectionsFormValidator definePipelineHuooSectionsFormValidator) {
    this.padPipelinesHuooService = padPipelinesHuooService;
    this.controllerHelperService = controllerHelperService;
    this.pickSplitPipelineFormValidator = pickSplitPipelineFormValidator;
    this.pickableHuooPipelineIdentService = pickableHuooPipelineIdentService;
    this.definePipelineHuooSectionsFormValidator = definePipelineHuooSectionsFormValidator;
  }

  @GetMapping("/select-pipeline")
  public ModelAndView renderSelectPipelineToSplit(@PathVariable("applicationType")
                                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                  @PathVariable("applicationId") int applicationId,
                                                  @PathVariable("huooRole") HuooRole huooRole,
                                                  PwaApplicationContext applicationContext,
                                                  @ModelAttribute("form") PickSplitPipelineForm form) {


    return getSelectPipelineModelAndView(applicationContext, huooRole);
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
          // if more than 1 section, continue split journey
          return ReverseRouter.redirect(on(SplitPipelineHuooJourneyController.class).renderDefineSections(
              pwaApplicationType,
              applicationId,
              huooRole,
              form.getPipelineId(),
              form.getNumberOfSections(),
              null, null
          ));
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

  @GetMapping("/pipeline/{pipelineId}/sections/{numberOfSections}")
  public ModelAndView renderDefineSections(@PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           @PathVariable("applicationId") int applicationId,
                                           @PathVariable("huooRole") HuooRole huooRole,
                                           @PathVariable("pipelineId") int pipelineId,
                                           @PathVariable("numberOfSections") int numberOfSections,
                                           PwaApplicationContext applicationContext,
                                           @ModelAttribute("form") DefinePipelineHuooSectionsForm form) {

    return withSplitablePipelineAndRole(
        pipelineId,
        numberOfSections,
        huooRole,
        applicationContext,
        splitablePipelineOverview -> {

          var pickableIdentLocationOptions = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(
              applicationContext.getApplicationDetail(), PipelineId.from(splitablePipelineOverview)
          );

          form.resetSectionPoints(numberOfSections, pickableIdentLocationOptions.get(0));

          return getDefineSectionModelAndView(
              applicationContext,
              huooRole,
              numberOfSections,
              splitablePipelineOverview,
              pickableIdentLocationOptions
          );
        }
    );

  }

  @PostMapping("/pipeline/{pipelineId}/sections/{numberOfSections}")
  public ModelAndView defineSections(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("applicationId") int applicationId,
                                     @PathVariable("huooRole") HuooRole huooRole,
                                     @PathVariable("pipelineId") int pipelineId,
                                     @PathVariable("numberOfSections") int numberOfSections,
                                     PwaApplicationContext applicationContext,
                                     @ModelAttribute("form") DefinePipelineHuooSectionsForm form,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {

    return withSplitablePipelineAndRole(
        pipelineId,
        numberOfSections,
        huooRole,
        applicationContext,
        splitablePipelineOverview -> {
          var pipelineIdObj = PipelineId.from(splitablePipelineOverview);
          var pickableIdentLocationOptions = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(
              applicationContext.getApplicationDetail(), pipelineIdObj
          );

          var validationHint = new DefinePipelineHuooSectionValidationHint(
              applicationContext.getApplicationDetail(),
              huooRole,
              pipelineIdObj,
              numberOfSections
          );

          definePipelineHuooSectionsFormValidator.validate(form, bindingResult, validationHint);

          return controllerHelperService.checkErrorsAndRedirect(
              bindingResult,
              getDefineSectionModelAndView(
                  applicationContext,
                  huooRole,
                  numberOfSections,
                  splitablePipelineOverview,
                  pickableIdentLocationOptions),
              () -> {
                var pipelineSections = pickableHuooPipelineIdentService.generatePipelineSectionsFromForm(
                    applicationContext.getApplicationDetail(),
                    pipelineIdObj,
                    form
                );

                padPipelinesHuooService.replacePipelineSectionsForPipelineAndRole(
                    applicationContext.getApplicationDetail(),
                    huooRole,
                    pipelineIdObj,
                    pipelineSections
                );

                FlashUtils.success(
                    redirectAttributes,
                    String.format("%s %s sections defined for pipeline %s",
                        numberOfSections,
                        huooRole.getDisplayText(),
                        splitablePipelineOverview.getPipelineName()
                    )
                );
                return ReverseRouter.redirect(on(PipelinesHuooController.class).renderSummary(
                    pwaApplicationType, applicationId, null));

              });
        }
    );

  }

  private ModelAndView getDefineSectionModelAndView(PwaApplicationContext applicationContext,
                                                    HuooRole huooRole,
                                                    int numberOfSections,
                                                    PipelineOverview splittablePipelineOverview,
                                                    List<PickableIdentLocationOption> pickableIdentLocationOptions) {
    var huooRoleDisplayText = huooRole.getDisplayText().toLowerCase();
    var optionsMap = pickableIdentLocationOptions.stream()
        .collect(StreamUtils.toLinkedHashMap(
            PickableIdentLocationOption::getPickableString,
            PickableIdentLocationOption::getDisplayString
        ));

    return new ModelAndView("pwaApplication/shared/pipelinehuoo/splitPipelineHuooDefineSections")
        .addObject("pickableIdentOptions", optionsMap)
        .addObject("backUrl", ReverseRouter.route(on(SplitPipelineHuooJourneyController.class).renderSelectPipelineToSplit(
            applicationContext.getApplicationType(),
            applicationContext.getMasterPwaApplicationId(),
            huooRole,
            null,
            null
            )
        ))
        .addObject(
            "firstSectionStartDescription",
            String.format("Section 1 is from and including %s", pickableIdentLocationOptions.get(0).getDisplayString())
        )
        .addObject(
            "lastSectionEndDescription",
            String.format(
                "Section %s is to and including %s",
                numberOfSections,
                pickableIdentLocationOptions.get(pickableIdentLocationOptions.size() - 1).getDisplayString()
            )
        )
        .addObject("backLinkText", DEFINE_SECTIONS_BACK_LINK_TEXT)
        .addObject("pageHeading",
            String.format(
                DEFINE_SECTIONS_PAGE_HEADING_FORMAT,
                splittablePipelineOverview.getPipelineName(),
                huooRoleDisplayText)
        )
        .addObject("totalSections", numberOfSections);

  }

  /**
   * Do belt and braces check on sanity of pipeline param.
   * Error when pipeline not "splittable" for app detail or sections < 2 as this doesnt make sense.
   */
  private ModelAndView withSplitablePipelineAndRole(int pipelineId,
                                                    int numberOfSections,
                                                    HuooRole huooRole,
                                                    PwaApplicationContext applicationContext,
                                                    Function<PipelineOverview, ModelAndView> modelAndViewFunction) {

    var pipelineIdObj = new PipelineId(pipelineId);
    var splitablePipeline = padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(
        applicationContext.getApplicationDetail(),
        pipelineIdObj
    );

    var assignableRoles = padPipelinesHuooService.countDistinctRoleOwnersForRole(
        applicationContext.getApplicationDetail(),
        huooRole
    );

    if (numberOfSections < 2) {
      throw new AccessDeniedException("Cannot define a pipeline split with less than 2 sections");
    }

    if (assignableRoles <= 1) {
      throw new AccessDeniedException("Cannot define a pipeline split with fewer than 2 assignable role owners");
    }

    return modelAndViewFunction.apply(splitablePipeline);

  }


}
