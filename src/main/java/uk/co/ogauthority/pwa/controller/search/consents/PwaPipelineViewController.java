package uk.co.ogauthority.pwa.controller.search.consents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.form.pwa.PwaPipelineHistoryForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;
import uk.co.ogauthority.pwa.service.search.consents.PwaPipelineViewTab;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.PwaPipelineHistoryViewService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaPipelineViewUrlFactory;

@Controller
@RequestMapping("/consents/pwa-view/{pwaId}/pipeline")
@PwaPermissionCheck(permissions = PwaPermission.VIEW_PWA)
public class PwaPipelineViewController {

  private final PipelineDetailService pipelineDetailService;
  private final PwaPipelineHistoryViewService pwaPipelineHistoryViewService;
  private final SearchPwaBreadcrumbService searchPwaBreadcrumbService;

  @Autowired
  public PwaPipelineViewController(PipelineDetailService pipelineDetailService,
                                   PwaPipelineHistoryViewService pwaPipelineHistoryViewService,
                                   SearchPwaBreadcrumbService searchPwaBreadcrumbService) {
    this.pipelineDetailService = pipelineDetailService;
    this.pwaPipelineHistoryViewService = pwaPipelineHistoryViewService;
    this.searchPwaBreadcrumbService = searchPwaBreadcrumbService;
  }


  @GetMapping("/{pipelineId}/{tab}")
  public ModelAndView renderViewPwaPipeline(@PathVariable("pwaId") Integer pwaId,
                                            @PathVariable("pipelineId") Integer pipelineId,
                                            @PathVariable("tab") PwaPipelineViewTab tab,
                                            PwaContext pwaContext,
                                            AuthenticatedUserAccount authenticatedUserAccount,
                                            @ModelAttribute("form") PwaPipelineHistoryForm form,
                                            @RequestParam(value = "pipelineDetailId", required = false) Integer pipelineDetailId) {

    return getModelAndView(tab, pwaContext, pipelineId, pipelineDetailId, form);
  }

  @PostMapping("/{pipelineId}/{tab}")
  public ModelAndView postViewPwaPipeline(@PathVariable("pwaId") Integer pwaId,
                                          @PathVariable("pipelineId") Integer pipelineId,
                                          @PathVariable("tab") PwaPipelineViewTab tab,
                                          PwaContext pwaContext,
                                          AuthenticatedUserAccount authenticatedUserAccount,
                                          @ModelAttribute("form") PwaPipelineHistoryForm form) {


    return ReverseRouter.redirect(on(PwaPipelineViewController.class)
        .renderViewPwaPipeline(pwaId, pipelineId, tab, pwaContext, authenticatedUserAccount, null, form.getPipelineDetailId()));
  }


  private ModelAndView getModelAndView(PwaPipelineViewTab tab,
                                       PwaContext pwaContext,
                                       Integer pipelineId,
                                       Integer pipelineDetailId,
                                       PwaPipelineHistoryForm form) {

    var latestPipelineDetail = pipelineDetailService.getLatestByPipelineId(pipelineId);

    var modelAndView =  new ModelAndView("search/consents/pwaPipelineView/pwaPipelineView")
        .addObject("consentSearchResultView", pwaContext.getConsentSearchResultView())
        .addObject("availableTabs", PwaPipelineViewTab.stream().collect(Collectors.toList()))
        .addObject("currentProcessingTab", tab)
        .addObject("pwaPipelineViewUrlFactory", new PwaPipelineViewUrlFactory(pwaContext.getMasterPwa().getId(), pipelineId))
        .addObject("pipelineReference", latestPipelineDetail.getPipelineNumber());


    if (tab.equals(PwaPipelineViewTab.PIPELINE_HISTORY)) {
      var selectedPipelineDetailId = pipelineDetailId;
      if (pipelineDetailId == null) {
        selectedPipelineDetailId = latestPipelineDetail.getId();
        form.setPipelineDetailId(latestPipelineDetail.getId());
      }
      setPipelineHistoryDataOnModelAndView(modelAndView, pwaContext, pipelineId, selectedPipelineDetailId);
    }

    searchPwaBreadcrumbService.fromPwaPipelineView(
        pwaContext.getMasterPwa().getId(), pwaContext.getConsentSearchResultView().getPwaReference(), modelAndView, "View pipeline");

    return modelAndView;
  }


  private void setPipelineHistoryDataOnModelAndView(ModelAndView modelAndView,
                                                    PwaContext pwaContext,
                                                    Integer pipelineId,
                                                    Integer pipelineDetailId) {

    var diffedPipelineSummaryModel = pwaPipelineHistoryViewService.getDiffedPipelineSummaryModel(pipelineDetailId, pipelineId);
    var viewPwaPipelineUrl  = ReverseRouter.route(on(PwaPipelineViewController.class).renderViewPwaPipeline(
        pwaContext.getMasterPwa().getId(), pipelineId, PwaPipelineViewTab.PIPELINE_HISTORY, pwaContext, null, null, null));
    var pipelinesVersionSearchSelectorItems = pwaPipelineHistoryViewService.getPipelinesVersionSearchSelectorItems(pipelineId);

    modelAndView.addObject("diffedPipelineSummaryModel", diffedPipelineSummaryModel)
        .addObject("viewPwaPipelineUrl", viewPwaPipelineUrl)
        .addObject("pipelinesVersionSearchSelectorItems", pipelinesVersionSearchSelectorItems)
        .addObject("unitMeasurements", UnitMeasurement.toMap());
  }



}