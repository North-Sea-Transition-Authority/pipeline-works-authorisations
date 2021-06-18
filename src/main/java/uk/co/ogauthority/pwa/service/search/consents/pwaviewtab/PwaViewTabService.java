package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentDtoRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;

@Service
public class PwaViewTabService {

  private final PipelineDetailService pipelineDetailService;
  private final PwaConsentDtoRepository pwaConsentDtoRepository;


  @Autowired
  public PwaViewTabService(PipelineDetailService pipelineDetailService,
                           PwaConsentDtoRepository pwaConsentDtoRepository) {
    this.pipelineDetailService = pipelineDetailService;
    this.pwaConsentDtoRepository = pwaConsentDtoRepository;
  }


  public Map<String, Object> getTabContentModelMap(PwaContext pwaContext,
                                              PwaViewTab tab) {

    Map<String, Object> tabContentMap = new HashMap<>();

    if (tab == PwaViewTab.PIPELINES) {
      tabContentMap.put("pwaPipelineViews", getPipelineTabContent(pwaContext));

    } else if (tab == PwaViewTab.CONSENT_HISTORY) {
      tabContentMap.put("pwaConsentHistoryViews", getConsentHistoryTabContent(pwaContext));
    }

    return tabContentMap;
  }


  private List<PwaPipelineView> getPipelineTabContent(PwaContext pwaContext) {

    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    var pipelineOverviews = pipelineDetailService.getCompletePipelineOverviewsForMasterPwaAndStatus(
        pwaContext.getMasterPwa(), pipelineStatusFilter);

    return pipelineOverviews
        .stream().map(PwaPipelineView::new)
        .sorted(Comparator.comparing(PwaPipelineView::getPipelineNumberOnlyFromReference))
        .collect(Collectors.toList());
  }


  private List<PwaConsentApplicationDto> getConsentHistoryTabContent(PwaContext pwaContext) {
    return pwaConsentDtoRepository.getConsentAndApplicationDtos(pwaContext.getMasterPwa())
        .stream()
        .sorted(Comparator.comparing(PwaConsentApplicationDto::getConsentInstant).reversed())
        .collect(Collectors.toList());
  }

}
