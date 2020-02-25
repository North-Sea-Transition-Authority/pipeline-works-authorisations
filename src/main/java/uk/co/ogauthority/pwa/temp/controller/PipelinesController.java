package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.components.PipelineGodObject;
import uk.co.ogauthority.pwa.temp.model.form.AddProductionPipelineForm;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;
import uk.co.ogauthority.pwa.temp.model.view.PipelineView;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.EnumUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@Scope("request")
@RequestMapping("/application/{applicationId}/pipelines")
public class PipelinesController {

  private PipelineGodObject pipelineGodObject;

  @Autowired
  public PipelinesController(PipelineGodObject pipelineGodObject) {
    this.pipelineGodObject = pipelineGodObject;
    if (pipelineGodObject.getPipelineViewList() == null || pipelineGodObject.getPipelineViewList().isEmpty()) {
      PipelineView firstPipeline = new PipelineView("PL123", PipelineType.PRODUCTION_FLOWLINE, List.of());
      firstPipeline.setLength(99);
      firstPipeline.setFrom("Schiehallion FPSO");
      firstPipeline.setFromLatitudeDegrees("1");
      firstPipeline.setFromLatitudeMinutes("2");
      firstPipeline.setFromLatitudeSeconds("3");
      firstPipeline.setFromLongitudeDegrees("3");
      firstPipeline.setFromLongitudeMinutes("2");
      firstPipeline.setFromLongitudeSeconds("1");
      firstPipeline.setToLatitudeDegrees("5");
      firstPipeline.setToLatitudeMinutes("4");
      firstPipeline.setToLatitudeSeconds("3");
      firstPipeline.setToLongitudeDegrees("10");
      firstPipeline.setToLongitudeMinutes("89");
      firstPipeline.setToLongitudeSeconds("77");
      firstPipeline.setTo("Sullom Voe Terminal");
      firstPipeline.setProductsToBeConveyed("Oil");
      firstPipeline.setComponentParts("Sullom Voe Terminal");
      firstPipeline.setSubPipelines(List.of());
      pipelineGodObject.setPipelineViewList(List.of(firstPipeline));
    }
  }

  @GetMapping
  public ModelAndView pipelines(@PathVariable Integer applicationId) {

    return new ModelAndView("pwaApplication/temporary/pipelines")
        .addObject("pipelineViews", pipelineGodObject.getPipelineViewList())
        .addObject("addProductionPipelineUrl",
            ReverseRouter.route(on(PipelinesController.class).addProductionPipelineRender(applicationId, null)))
        .addObject("viewEditPipelineUrl",
            ReverseRouter.route(on(PipelinesController.class).editProductionPipelineRender(applicationId, null)))
        .addObject("saveCompleteLaterUrl", ReverseRouter.route(on(PwaApplicationController.class).viewTaskList(applicationId)));
  }

  @PostMapping
  public ModelAndView pipelinesComplete(@PathVariable Integer applicationId) {
    return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(applicationId)); // TODO point to location when merged in
  }

  @GetMapping("/add-production-pipeline")
  public ModelAndView addProductionPipelineRender(@PathVariable Integer applicationId,
                                                  @ModelAttribute("form") AddProductionPipelineForm form) {
    return getAddProductionPipelineMav(applicationId, form);
  }

  private ModelAndView getAddProductionPipelineMav(Integer applicationId, AddProductionPipelineForm form) {
    return new ModelAndView("pwaApplication/temporary/addProductionPipeline")
        .addObject("pipelineTypes", Arrays.stream(PipelineType.values())
            .filter(PipelineType::isRootPipelineType)
            .collect(StreamUtils.toLinkedHashMap(Enum::name, PipelineType::getDisplayName)))
        .addObject("form", form)
        .addObject("cancelUrl", ReverseRouter.route(on(PipelinesController.class).pipelines(applicationId)));
  }

  @PostMapping("add-production-pipeline")
  public ModelAndView addProductionPipeline(@PathVariable Integer applicationId,
                                            @Valid @ModelAttribute("form") AddProductionPipelineForm form,
                                            BindingResult bindingResult) {

    return ControllerUtils.validateAndRedirect(bindingResult, getAddProductionPipelineMav(applicationId, form), () -> {

      PipelineView pipelineView = new PipelineView();
      pipelineView.setPipelineType(EnumUtils.getEnumValue(PipelineType.class, form.getPipelineType()));
      pipelineView.setFrom(form.getFrom());
      pipelineView.setFromLatitudeDegrees(form.getFromLatitudeDegrees());
      pipelineView.setFromLatitudeMinutes(form.getFromLatitudeMinutes());
      pipelineView.setFromLatitudeSeconds(form.getFromLatitudeSeconds());
      pipelineView.setFromLongitudeDegrees(form.getFromLongitudeDegrees());
      pipelineView.setFromLongitudeMinutes(form.getFromLongitudeMinutes());
      pipelineView.setFromLongitudeSeconds(form.getFromLongitudeSeconds());
      pipelineView.setTo(form.getTo());
      pipelineView.setToLatitudeDegrees(form.getToLatitudeDegrees());
      pipelineView.setToLatitudeMinutes(form.getToLatitudeMinutes());
      pipelineView.setToLatitudeSeconds(form.getToLatitudeSeconds());
      pipelineView.setToLongitudeDegrees(form.getToLongitudeDegrees());
      pipelineView.setToLongitudeMinutes(form.getToLongitudeMinutes());
      pipelineView.setToLongitudeSeconds(form.getToLongitudeSeconds());
      pipelineView.setComponentParts(form.getComponentParts());
      pipelineView.setProductsToBeConveyed(form.getProductsToBeConveyed());
      pipelineView.setLength(form.getLength());

      pipelineView.setSubPipelines(List.of());

      String newPipelineNumber = "PL" + new Random().ints(1, 1, 10000).findFirst().getAsInt();
      pipelineView.setPipelineNumber(newPipelineNumber);

      List<PipelineView> views = new ArrayList<>(pipelineGodObject.getPipelineViewList());
      views.add(pipelineView);
      pipelineGodObject.setPipelineViewList(views);

      return ReverseRouter.redirect(on(PipelinesController.class).editProductionPipelineRender(applicationId, newPipelineNumber));

    });

  }

  @GetMapping("/production/{pipelineNumber}")
  public ModelAndView editProductionPipelineRender(@PathVariable Integer applicationId,
                                                   @PathVariable String pipelineNumber) {

    return new ModelAndView("pwaApplication/temporary/productionPipeline")
        .addObject("pipelineView", pipelineGodObject.getPipelineViewList().stream()
          .filter(v -> v.getPipelineNumber().equals(pipelineNumber))
          .findFirst()
          .orElseThrow())
        .addObject("addIdentUrl", ReverseRouter.route(on(PipelinesController.class).addIdentRender(applicationId, pipelineNumber)))
        .addObject("backToPipelinesUrl", ReverseRouter.route(on(PipelinesController.class).pipelines(applicationId)));

  }

  @GetMapping("/production/{pipelineNumber}/ident/new")
  public ModelAndView addIdentRender(@PathVariable Integer applicationId,
                                     @PathVariable String pipelineNumber) {
    return new ModelAndView(); // TODO IN NEXT JIRA
  }

  @PostMapping("/production/{pipelineNumber}/ident/new")
  public ModelAndView addIdent(@PathVariable Integer applicationId,
                               @PathVariable String pipelineNumber) {

    // add ident to list // TODO IN NEXT JIRA

    return ReverseRouter.redirect(on(PipelinesController.class).editProductionPipelineRender(applicationId, pipelineNumber));
  }

}
