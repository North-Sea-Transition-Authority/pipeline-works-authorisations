package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;

@Service
public class PwaViewTabService {

  private final PipelineDetailService pipelineDetailService;

  @Autowired
  public PwaViewTabService(PipelineDetailService pipelineDetailService) {
    this.pipelineDetailService = pipelineDetailService;
  }


  public Map<String, ?> getTabContentModelMap(PwaContext pwaContext,
                                              PwaViewTab tab) {

    Map<String, Object> tabContentMap = new HashMap<>();

    if (tab == PwaViewTab.PIPELINES) {
      tabContentMap.put("pwaPipelineViews", getPipelineTabContent(pwaContext));
    }

    return tabContentMap;

  }


  private List<PwaPipelineView> getPipelineTabContent(PwaContext pwaContext) {
    var pipelineOverviews = pipelineDetailService.getAllPipelineOverviewsForMasterPwa(pwaContext.getMasterPwa());
    return pipelineOverviews
        .stream().map(PwaPipelineView::new)
        .sorted(Comparator.comparing(PwaPipelineView::getPipelineNumberOnlyFromReference))
        .collect(Collectors.toList());
  }

}
