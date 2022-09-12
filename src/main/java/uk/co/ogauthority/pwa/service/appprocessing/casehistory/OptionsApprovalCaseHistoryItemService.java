package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;

@Service
public class OptionsApprovalCaseHistoryItemService implements CaseHistoryItemService {

  private final ApproveOptionsService service;

  @Autowired
  public OptionsApprovalCaseHistoryItemService(ApproveOptionsService service) {
    this.service = service;
  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {
    var optionalApproval =  service.getOptionsApproval(pwaApplication);

    if (optionalApproval.isPresent()) {
      var approval = optionalApproval.get();
      var builder = new CaseHistoryItemView.Builder(
          "Application options approved",
          approval.getCreatedTimestamp(),
          approval.getCreatedByPersonId())
          .setPersonLabelText("Approved by")
          .setPersonEmailLabel("Contact email");
      return List.of(builder.build());
    }
    return Collections.emptyList();
  }
}
