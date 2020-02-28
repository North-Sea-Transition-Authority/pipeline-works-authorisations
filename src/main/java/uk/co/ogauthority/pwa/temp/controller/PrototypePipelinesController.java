package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.temp.PrototypeApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.temp.components.PipelineGodObject;
import uk.co.ogauthority.pwa.temp.model.form.AddIdentForm;
import uk.co.ogauthority.pwa.temp.model.form.AddProductionPipelineForm;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;
import uk.co.ogauthority.pwa.temp.model.view.IdentView;
import uk.co.ogauthority.pwa.temp.model.view.PipelineView;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.EnumUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@Scope("request")
@RequestMapping("/prototype/application/{applicationId}/pipelines")
public class PrototypePipelinesController {

  private PipelineGodObject pipelineGodObject;
  private final PrototypeApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public PrototypePipelinesController(PipelineGodObject pipelineGodObject,
                                      PrototypeApplicationBreadcrumbService breadcrumbService) {
    this.pipelineGodObject = pipelineGodObject;
    this.breadcrumbService = breadcrumbService;
    if (pipelineGodObject.getPipelineViewList() == null || pipelineGodObject.getPipelineViewList().isEmpty()) {
      PipelineView firstPipeline = new PipelineView("PL1", PipelineType.PRODUCTION_FLOWLINE, List.of());
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
      firstPipeline.setIdents(List.of());
      pipelineGodObject.setPipelineViewList(List.of(firstPipeline));
    }
  }

  @GetMapping
  public ModelAndView pipelines(@PathVariable Integer applicationId) {

    var modelAndView = new ModelAndView("pwaApplication/temporary/pipelines")
        .addObject("pipelineViews", pipelineGodObject.getPipelineViewList().stream()
          .sorted(Comparator.comparingInt(p -> p.getPipelineType().getDisplayOrder()))
          .collect(Collectors.toList()))
        .addObject("addProductionPipelineUrl",
            ReverseRouter.route(on(PrototypePipelinesController.class).addProductionPipelineRender(applicationId, null)))
        .addObject("viewEditPipelineUrl",
            ReverseRouter.route(on(PrototypePipelinesController.class).editProductionPipelineRender(applicationId, null)))
        .addObject("saveCompleteLaterUrl", ReverseRouter.route(on(PrototypePwaApplicationController.class).viewTaskList(applicationId)));

    breadcrumbService.fromTaskList(applicationId, modelAndView, "Pipelines");
    return modelAndView;
  }

  @PostMapping
  public ModelAndView pipelinesComplete(@PathVariable Integer applicationId) {
    // TODO point to location when merged in
    return ReverseRouter.redirect(on(PrototypePwaApplicationController.class).viewTaskList(applicationId));
  }

  @GetMapping("/add-production-pipeline")
  public ModelAndView addProductionPipelineRender(@PathVariable Integer applicationId,
                                                  @ModelAttribute("form") AddProductionPipelineForm form) {
    return getAddProductionPipelineMav(applicationId, form);
  }

  private ModelAndView getAddProductionPipelineMav(Integer applicationId, AddProductionPipelineForm form) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/addProductionPipeline")
        .addObject("pipelineTypes", Arrays.stream(PipelineType.values())
            .collect(StreamUtils.toLinkedHashMap(Enum::name, PipelineType::getDisplayName)))
        .addObject("form", form)
        .addObject("cancelUrl", ReverseRouter.route(on(PrototypePipelinesController.class).pipelines(applicationId)));
    breadcrumbService.fromPipelines(applicationId, modelAndView, "Add pipeline");
    return modelAndView;
  }

  @PostMapping("/add-production-pipeline")
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

      pipelineView.setIdents(List.of());

      String newPipelineNumber = "PL" + pipelineGodObject.getPipelineViewList().size() + 1;
      pipelineView.setPipelineNumber(newPipelineNumber);

      List<PipelineView> views = new ArrayList<>(pipelineGodObject.getPipelineViewList());
      views.add(pipelineView);
      pipelineGodObject.setPipelineViewList(views);

      return ReverseRouter.redirect(on(PrototypePipelinesController.class).editProductionPipelineRender(applicationId, newPipelineNumber));

    });

  }

  @GetMapping("/production/{pipelineNumber}")
  public ModelAndView editProductionPipelineRender(@PathVariable Integer applicationId,
                                                   @PathVariable String pipelineNumber) {

    var modelAndView = new ModelAndView("pwaApplication/temporary/productionPipeline")
        .addObject("pipelineView", getPipelineOrThrow(pipelineNumber))
        .addObject("addIdentUrl", ReverseRouter.route(on(PrototypePipelinesController.class)
            .addIdentRender(applicationId, pipelineNumber, null)))
        .addObject("backToPipelinesUrl", ReverseRouter.route(on(PrototypePipelinesController.class).pipelines(applicationId)));

    breadcrumbService.fromPipelines(applicationId, modelAndView, pipelineNumber);
    return modelAndView;
  }

  @GetMapping("/production/{pipelineNumber}/ident/new")
  public ModelAndView addIdentRender(@PathVariable Integer applicationId,
                                     @PathVariable String pipelineNumber,
                                     @ModelAttribute("form") AddIdentForm form) {
    return getAddIdentMav(applicationId, pipelineNumber);
  }

  private ModelAndView getAddIdentMav(Integer applicationId, String pipelineNumber) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/addIdent")
        .addObject("identNo", getPipelineOrThrow(pipelineNumber).getIdents().size() + 1)
        .addObject("cancelUrl",
            ReverseRouter.route(on(PrototypePipelinesController.class).editProductionPipelineRender(applicationId, pipelineNumber)));

    breadcrumbService.fromPipeline(applicationId, pipelineNumber, modelAndView, "Add ident");
    return modelAndView;
  }

  @PostMapping("/production/{pipelineNumber}/ident/new")
  public ModelAndView addIdent(@PathVariable Integer applicationId,
                               @PathVariable String pipelineNumber,
                               @Valid @ModelAttribute("form") AddIdentForm form,
                               BindingResult bindingResult) {

    return ControllerUtils.validateAndRedirect(bindingResult, getAddIdentMav(applicationId, pipelineNumber), () -> {

      PipelineView prodPipeline = getPipelineOrThrow(pipelineNumber);

      List<IdentView> idents = new ArrayList<>(prodPipeline.getIdents());

      var newIdent = new IdentView();
      newIdent.setFrom(form.getFrom());
      newIdent.setFromLatitudeDegrees(form.getFromLatitudeDegrees());
      newIdent.setFromLatitudeMinutes(form.getFromLatitudeMinutes());
      newIdent.setFromLatitudeSeconds(form.getFromLatitudeSeconds());
      newIdent.setFromLongitudeDegrees(form.getFromLongitudeDegrees());
      newIdent.setFromLongitudeMinutes(form.getFromLongitudeMinutes());
      newIdent.setFromLongitudeSeconds(form.getFromLongitudeSeconds());
      newIdent.setTo(form.getTo());
      newIdent.setToLatitudeDegrees(form.getToLatitudeDegrees());
      newIdent.setToLatitudeMinutes(form.getToLatitudeMinutes());
      newIdent.setToLatitudeSeconds(form.getToLatitudeSeconds());
      newIdent.setToLongitudeDegrees(form.getToLongitudeDegrees());
      newIdent.setToLongitudeMinutes(form.getToLongitudeMinutes());
      newIdent.setToLongitudeSeconds(form.getToLongitudeSeconds());
      newIdent.setComponentParts(form.getComponentParts());
      newIdent.setProductsToBeConveyed(form.getProductsToBeConveyed());
      newIdent.setLength(form.getLength());
      newIdent.setExternalDiameter(form.getExternalDiameter());
      newIdent.setInternalDiameter(form.getInternalDiameter());
      newIdent.setWallThickness(form.getWallThickness());
      newIdent.setTypeOfInsulationOrCoating(form.getTypeOfInsulationOrCoating());
      newIdent.setMaop(form.getMaop());
      newIdent.setIdentNo(idents.size() + 1);

      idents.add(newIdent);

      prodPipeline.setIdents(idents);

      return ReverseRouter.redirect(on(PrototypePipelinesController.class).editProductionPipelineRender(applicationId, pipelineNumber));

    });

  }

  private PipelineView getPipelineOrThrow(String pipelineNumber) {
    return pipelineGodObject.getPipelineViewList().stream()
        .filter(v -> v.getPipelineNumber().equals(pipelineNumber))
        .findFirst()
        .orElseThrow();
  }

}
