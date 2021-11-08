package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class WithdrawApplicationCaseHistoryItemService implements CaseHistoryItemService {

  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public WithdrawApplicationCaseHistoryItemService(PwaApplicationDetailService pwaApplicationDetailService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {

    var appDetails = pwaApplicationDetailService.getAllWithdrawnApplicationDetailsForApplication(pwaApplication)
        .stream()
        .sorted(Comparator.comparing(PwaApplicationDetail::getVersionNo))
        .collect(Collectors.toList());

    return appDetails.stream()
        .map(pwaApplicationDetail -> new CaseHistoryItemView.Builder(
            "Application withdrawn",
                pwaApplicationDetail.getWithdrawalTimestamp(),
                pwaApplicationDetail.getWithdrawingPersonId()
            )
                .setPersonLabelText("Withdrawn by")
                .setPersonEmailLabel("Contact email")
                .build()
        )
        .collect(Collectors.toList());

  }

}
