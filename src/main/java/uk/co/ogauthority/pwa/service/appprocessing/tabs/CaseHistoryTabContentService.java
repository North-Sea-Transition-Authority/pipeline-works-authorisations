package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;

@Service
public class CaseHistoryTabContentService implements AppProcessingTabContentService {

  private final CaseHistoryService caseHistoryService;

  @Autowired
  public CaseHistoryTabContentService(CaseHistoryService caseHistoryService) {
    this.caseHistoryService = caseHistoryService;
  }

  @Override
  public Map<String, Object> getTabContent(PwaAppProcessingContext appProcessingContext, AppProcessingTab currentTab) {

    List<CaseHistoryItemView> historyItems = List.of();

    if (currentTab == AppProcessingTab.CASE_HISTORY) {
      historyItems = caseHistoryService.getCaseHistory(appProcessingContext.getPwaApplication());
    }

    return Map.of("caseHistoryItems", historyItems);

  }

}
