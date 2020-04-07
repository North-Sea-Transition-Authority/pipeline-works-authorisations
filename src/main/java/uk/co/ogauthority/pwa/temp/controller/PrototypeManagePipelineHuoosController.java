package uk.co.ogauthority.pwa.temp.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.temp.PrototypeApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.temp.components.PipelineGodObject;
import uk.co.ogauthority.pwa.temp.model.form.HuooCategoryForm;
import uk.co.ogauthority.pwa.temp.model.form.PipelineHoldersForm;
import uk.co.ogauthority.pwa.temp.model.form.SelectPipelinesForm;
import uk.co.ogauthority.pwa.temp.model.view.SelectPipelineView;
import uk.co.ogauthority.pwa.temp.service.enums.HuooCategory;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@Scope("request")
@RequestMapping("/prototype/application/{applicationId}/pipelines/manage-organisations")
public class PrototypeManagePipelineHuoosController {

  private PipelineGodObject pipelineGodObject;
  private final PrototypeApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public PrototypeManagePipelineHuoosController(PipelineGodObject pipelineGodObject,
                                                PrototypeApplicationBreadcrumbService breadcrumbService) {
    this.pipelineGodObject = pipelineGodObject;
    this.breadcrumbService = breadcrumbService;
  }

  @GetMapping
  public ModelAndView selectPipelines(Integer applicationId,
                                      @ModelAttribute("form") SelectPipelinesForm form,
                                      @RequestParam("multiple") boolean multipleSelection) {

    var pipes = pipelineGodObject.getPipelineViewList().stream()
        .map(SelectPipelineView::new)
        .collect(Collectors.toList());

    var modelAndView = new ModelAndView("pwaApplication/temporary/pipelineHuoos/selectPipelines")
        .addObject("multipleSelection", multipleSelection)
        .addObject("multipleSelectionUrl",
            ReverseRouter.route(on(PrototypeManagePipelineHuoosController.class).selectPipelines(applicationId,null, true)))
        .addObject("cancelSelectionUrl",
            ReverseRouter.route(on(PrototypeManagePipelineHuoosController.class).selectPipelines(applicationId, null, false)))
        .addObject("pipes", pipes);

    breadcrumbService.fromPipelines(applicationId, modelAndView, "Manage pipeline organisations");

    return modelAndView;

  }

  @PostMapping
  public ModelAndView selectPipelinesPost(Integer applicationId) {

    return ReverseRouter.redirect(on(PrototypeManagePipelineHuoosController.class).selectCategories(applicationId, null));

  }

  @GetMapping("/categories")
  public ModelAndView selectCategories(Integer applicationId,
                                       @ModelAttribute("form") HuooCategoryForm form) {

    var modelAndView = new ModelAndView("pwaApplication/temporary/pipelineHuoos/selectCategories")
        .addObject("categoryOptions", HuooCategory.stream()
          .collect(StreamUtils.toLinkedHashMap(Enum::name, HuooCategory::getDisplayName)));

    breadcrumbService.fromPipelines(applicationId, modelAndView, "Select HUOO categories");

    return modelAndView;

  }

  @PostMapping("/categories")
  public ModelAndView selectCategoriesPost(Integer applicationId,
                                           @ModelAttribute("form") HuooCategoryForm form,
                                           BindingResult bindingResult) {

    return ReverseRouter.redirect(on(PrototypeManagePipelineHuoosController.class).renderHolders(applicationId, null, false));

  }

  @GetMapping("/holders")
  public ModelAndView renderHolders(Integer applicationId,
                                    @ModelAttribute("form") PipelineHoldersForm form,
                                    @RequestParam("edit") boolean editing) {

    var modelAndView = new ModelAndView("pwaApplication/temporary/pipelineHuoos/pipelineHolders")
          .addObject("enableBreadcrumbs", false);

    if (!editing) {
      breadcrumbService.fromPipelines(applicationId, modelAndView, "Update holders");
      modelAndView.addObject("enableBreadcrumbs", true);
    }

    return modelAndView;

  }

  @PostMapping("/holders")
  public ModelAndView updateHolders(Integer applicationId,
                                    @ModelAttribute("form") PipelineHoldersForm form,
                                    BindingResult bindingResult,
                                    @RequestParam("edit") boolean editing) {

    if (editing) {
      return ReverseRouter.redirect(on(PrototypeManagePipelineHuoosController.class).checkAnswers(applicationId));
    }

    return ReverseRouter.redirect(on(PrototypeManagePipelineHuoosController.class).renderUsers(applicationId, null, false));

  }

  @GetMapping("/users")
  public ModelAndView renderUsers(Integer applicationId,
                                  @ModelAttribute("form") PipelineHoldersForm form,
                                  @RequestParam("edit") boolean editing) {

    var modelAndView = new ModelAndView("pwaApplication/temporary/pipelineHuoos/pipelineUsers");

    if (!editing) {
      breadcrumbService.fromPipelines(applicationId, modelAndView, "Update users");
    }

    return modelAndView;

  }

  @PostMapping("/users")
  public ModelAndView updateUsers(Integer applicationId,
                                  @ModelAttribute("form") PipelineHoldersForm form,
                                  BindingResult bindingResult,
                                  @RequestParam("edit") boolean editing) {

    return ReverseRouter.redirect(on(PrototypeManagePipelineHuoosController.class).checkAnswers(applicationId));

  }

  @GetMapping("/changes")
  public ModelAndView checkAnswers(Integer applicationId) {

    return new ModelAndView("pwaApplication/temporary/pipelineHuoos/checkAnswers")
        .addObject("changeHoldersUrl",
            ReverseRouter.route(on(PrototypeManagePipelineHuoosController.class).renderHolders(applicationId, null, true)));

  }

  @PostMapping("/changes")
  public ModelAndView saveAndReturn(Integer applicationId) {

    return ReverseRouter.redirect(on(PrototypeManagePipelineHuoosController.class).selectPipelines(applicationId, null, false));

  }

}
