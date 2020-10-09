package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import java.util.Map;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;

public interface AppProcessingTabContentService {

  Map<String, Object> getTabContent(PwaAppProcessingContext appProcessingContext, AppProcessingTab currentTab);

}