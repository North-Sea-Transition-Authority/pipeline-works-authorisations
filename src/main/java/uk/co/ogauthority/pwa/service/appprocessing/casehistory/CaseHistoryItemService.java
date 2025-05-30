package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;

public interface CaseHistoryItemService {

  List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication);

}
