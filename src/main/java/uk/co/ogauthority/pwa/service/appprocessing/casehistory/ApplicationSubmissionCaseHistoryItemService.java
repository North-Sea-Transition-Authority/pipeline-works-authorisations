package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class ApplicationSubmissionCaseHistoryItemService implements CaseHistoryItemService {

  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public ApplicationSubmissionCaseHistoryItemService(PwaApplicationDetailService pwaApplicationDetailService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {

    var appDetails = pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(pwaApplication)
        .stream()
        .sorted(Comparator.comparing(PwaApplicationDetail::getVersionNo))
        .collect(Collectors.toList());

    return appDetails.stream()
        .map(pwaApplicationDetail -> new CaseHistoryItemView.Builder(
                String.format("Application version %s submitted", pwaApplicationDetail.getVersionNo()),
                pwaApplicationDetail.getSubmittedTimestamp(),
                pwaApplicationDetail.getSubmittedByPersonId()
            )
                .setPersonLabelText("Submitted by")
                .setPersonEmailLabel("Contact email")
                .build()
        )
        .collect(Collectors.toList());

  }

}
