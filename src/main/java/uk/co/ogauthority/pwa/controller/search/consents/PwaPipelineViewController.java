package uk.co.ogauthority.pwa.controller.search.consents;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
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
                                            AuthenticatedUserAccount authenticatedUserAccount) {

    var pipelineDetail = pipelineDetailService.getLatestByPipelineId(pipelineId);

    var modelAndView =  new ModelAndView("search/consents/pwaPipelineView/pwaPipelineView")
        .addObject("consentSearchResultView", pwaContext.getConsentSearchResultView())
        .addObject("availableTabs", PwaPipelineViewTab.stream().collect(Collectors.toList()))
        .addObject("currentProcessingTab", tab)
        .addObject("pwaPipelineViewUrlFactory", new PwaPipelineViewUrlFactory(pwaContext.getMasterPwa().getId(), pipelineId))
        .addObject("pipelineReference", pipelineDetail.getPipelineNumber());


    if (tab.equals(PwaPipelineViewTab.PIPELINE_HISTORY)) {
      setPipelineHistoryDataOnModelAndView(pipelineId, modelAndView);
    }

    searchPwaBreadcrumbService.fromPwaPipelineView(
        pwaId, pwaContext.getConsentSearchResultView().getPwaReference(), modelAndView, "View pipeline");

    return modelAndView;
  }


  private void setPipelineHistoryDataOnModelAndView(Integer pipelineId, ModelAndView modelAndView) {

    var diffedPipelineSummaryModel = pwaPipelineHistoryViewService.getDiffedPipelineSummaryModel(pipelineId);
    modelAndView.addObject("diffedPipelineSummaryModel", diffedPipelineSummaryModel);
    modelAndView.addObject("unitMeasurements", UnitMeasurement.toMap());
  }



}