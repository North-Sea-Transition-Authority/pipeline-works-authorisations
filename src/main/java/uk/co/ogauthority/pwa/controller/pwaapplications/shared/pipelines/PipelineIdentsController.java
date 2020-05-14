package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines/pipeline/{padPipelineId}/idents")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class PipelineIdentsController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final PipelineIdentFormValidator validator;
  private final PadPipelineIdentService padIdentService;

  @Autowired
  public PipelineIdentsController(ApplicationBreadcrumbService breadcrumbService,
                                  PipelineIdentFormValidator validator,
                                  PadPipelineIdentService padIdentService) {
    this.breadcrumbService = breadcrumbService;
    this.validator = validator;
    this.padIdentService = padIdentService;
  }

  private ModelAndView getIdentOverviewModelAndView(PwaApplicationDetail detail, PadPipeline padPipeline) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/identOverview")
        .addObject("pipelineOverview", new PipelineOverview(padPipeline, List.of()))
        .addObject("groupedIdentViews", padIdentService.getGroupedIdentViews(padPipeline))
        .addObject("addIdentUrl", ReverseRouter.route(on(PipelineIdentsController.class)
            .renderAddIdent(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), padPipeline.getId(), null, null)));

    breadcrumbService.fromPipelinesOverview(detail.getPwaApplication(), modelAndView, padPipeline.getPipelineRef() + " idents");

    return modelAndView;

  }

  @GetMapping
  public ModelAndView renderIdentOverview(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          @PathVariable("padPipelineId") Integer padPipelineId,
                                          PwaApplicationContext applicationContext) {
    return getIdentOverviewModelAndView(applicationContext.getApplicationDetail(), applicationContext.getPadPipeline());
  }

  private ModelAndView getAddIdentModelAndView(PwaApplicationDetail detail, PipelineIdentForm identForm, PadPipeline padPipeline) {
    var modelAndView = new ModelAndView("pwaApplication/shared/pipelines/addEditIdent")
        .addObject("longDirections", LongitudeDirection.stream()
            .collect(StreamUtils.toLinkedHashMap(Enum::name, LongitudeDirection::getDisplayText)))
        .addObject("cancelUrl", ReverseRouter.route(on(PipelineIdentsController.class)
            .renderIdentOverview(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), padPipeline.getId(), null)))
        .addObject("screenActionType", ScreenActionType.ADD)
        .addObject("form", identForm);

    breadcrumbService.fromPipelineIdentOverview(detail.getPwaApplication(), padPipeline, modelAndView, "Add ident");
    return modelAndView;
  }

  @GetMapping("/add")
  public ModelAndView renderAddIdent(@PathVariable("applicationId") Integer applicationId,
                                     @PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("padPipelineId") Integer padPipelineId,
                                     PwaApplicationContext applicationContext,
                                     @ModelAttribute("form") PipelineIdentForm form) {

    // set the fromLocation of our new ident to the toLocation of the previous ident if one exists
    padIdentService.getMaxIdent(applicationContext.getPadPipeline())
        .ifPresent(previousIdent -> form.setFromLocation(previousIdent.getToLocation()));

    return getAddIdentModelAndView(applicationContext.getApplicationDetail(), form, applicationContext.getPadPipeline());

  }

  @PostMapping("/add")
  public ModelAndView postAddIdent(@PathVariable("applicationId") Integer applicationId,
                                   @PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   @PathVariable("padPipelineId") Integer padPipelineId,
                                   PwaApplicationContext applicationContext,
                                   @ModelAttribute("form") PipelineIdentForm form,
                                   BindingResult bindingResult) {

    validator.validate(form, bindingResult, applicationContext);

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        getAddIdentModelAndView(applicationContext.getApplicationDetail(), form, applicationContext.getPadPipeline()), () -> {

          padIdentService.addIdent(applicationContext.getPadPipeline(), form);
          return ReverseRouter.redirect(on(PipelineIdentsController.class).renderIdentOverview(
              applicationId, pwaApplicationType, padPipelineId, applicationContext));

        });

  }

}
