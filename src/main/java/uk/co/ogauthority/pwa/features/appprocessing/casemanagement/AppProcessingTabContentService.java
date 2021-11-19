package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import java.util.Map;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;

public interface AppProcessingTabContentService {

  Map<String, Object> getTabContent(PwaAppProcessingContext appProcessingContext, AppProcessingTab currentTab);

}