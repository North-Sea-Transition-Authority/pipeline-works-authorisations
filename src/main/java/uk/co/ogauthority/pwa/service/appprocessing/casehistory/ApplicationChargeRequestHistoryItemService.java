package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;

@Service
public class ApplicationChargeRequestHistoryItemService implements CaseHistoryItemService {

  private final ApplicationChargeRequestService applicationChargeRequestService;

  @Autowired
  public ApplicationChargeRequestHistoryItemService(ApplicationChargeRequestService applicationChargeRequestService) {
    this.applicationChargeRequestService = applicationChargeRequestService;
  }


  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {
    return null;
  }
}
