package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.temp.components.PipelineGodObject;
import uk.co.ogauthority.pwa.temp.components.TechnicalDrawingsGodObject;
import uk.co.ogauthority.pwa.temp.model.form.DrawingLinkForm;
import uk.co.ogauthority.pwa.temp.model.view.TechnicalDrawingView;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@Scope("request")
@RequestMapping("/application/{applicationId}/technical-drawings")
public class TechnicalDrawingsController {

  private PipelineGodObject pipelineGodObject;
  private TechnicalDrawingsGodObject technicalDrawingsGodObject;
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @Autowired
  public TechnicalDrawingsController(PipelineGodObject pipelineGodObject,
                                     TechnicalDrawingsGodObject technicalDrawingsGodObject,
                                     ApplicationBreadcrumbService applicationBreadcrumbService) {
    this.pipelineGodObject = pipelineGodObject;
    this.technicalDrawingsGodObject = technicalDrawingsGodObject;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    technicalDrawingsGodObject.setTechnicalDrawingViews(List.of(
        new TechnicalDrawingView(1, "/assets/temp/images/technical_drawing_ex1.png", List.of(), "Detailed example"),
        new TechnicalDrawingView(2, "/assets/temp/images/technical_drawing_ex2.png", pipelineGodObject.getPipelineViewList(),
            "Simple overview")
    ));
  }

  @GetMapping
  public ModelAndView viewTechnicalDrawings(@PathVariable("applicationId") Integer applicationId) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/technicalDrawings/drawings")
        .addObject("technicalDrawings", technicalDrawingsGodObject.getTechnicalDrawingViews()
            .stream().collect(StreamUtils.toLinkedHashMap(technicalDrawingView -> technicalDrawingView,
                technicalDrawingView -> technicalDrawingView.getEditRoute(applicationId))))
        .addObject("backLinkUrl", ReverseRouter.route(on(PwaApplicationController.class).viewTaskList(applicationId)));
    applicationBreadcrumbService.fromTaskList(applicationId, modelAndView, "Technical drawings");
    return modelAndView;
  }

  @GetMapping("/{drawingId}/link")
  public ModelAndView viewDrawingEdit(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("drawingId") Integer drawingId,
                                      @ModelAttribute("form") DrawingLinkForm drawingLinkForm) {
    var modelAndView = new ModelAndView("pwaApplication/temporary/technicalDrawings/links")
        .addObject("pipelineViews", pipelineGodObject.getPipelineViewList()
            .stream()
            .collect(StreamUtils.toLinkedHashMap(pipelineView -> String.valueOf(pipelineView.hashCode()), pipelineView -> pipelineView)))
        .addObject("saveCompleteLaterUrl", ReverseRouter.route(on(TechnicalDrawingsController.class).viewTechnicalDrawings(applicationId)));
    applicationBreadcrumbService.fromTechnicalDrawings(applicationId, modelAndView, getDrawingOrThrow(drawingId).getName());
    return modelAndView;
  }

  @PostMapping("/{drawingId}/link")
  public ModelAndView postDrawingEdit(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("drawingId") Integer drawingId,
                                      @ModelAttribute("form") DrawingLinkForm drawingLinkForm) {
    return ReverseRouter.redirect(on(TechnicalDrawingsController.class).viewTechnicalDrawings(applicationId));
  }

  private TechnicalDrawingView getDrawingOrThrow(Integer id) {
    return technicalDrawingsGodObject.getTechnicalDrawingViews()
        .stream()
        .filter(technicalDrawingView -> technicalDrawingView.getDrawingId().equals(id))
        .findFirst()
        .orElseThrow();
  }

}
