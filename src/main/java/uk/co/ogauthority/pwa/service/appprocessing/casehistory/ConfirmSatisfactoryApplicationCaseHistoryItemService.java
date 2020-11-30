package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class ConfirmSatisfactoryApplicationCaseHistoryItemService implements CaseHistoryItemService {

  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public ConfirmSatisfactoryApplicationCaseHistoryItemService(PwaApplicationDetailService pwaApplicationDetailService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {

    var appDetails = pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(pwaApplication)
        .stream()
        .filter(detail -> detail.getConfirmedSatisfactoryTimestamp() != null)
        .sorted(Comparator.comparing(PwaApplicationDetail::getVersionNo))
        .collect(Collectors.toList());

    return appDetails.stream()
        .map(pwaApplicationDetail -> {

          var builder = new CaseHistoryItemView.Builder(
              String.format("Application version %s confirmed satisfactory", pwaApplicationDetail.getVersionNo()),
              pwaApplicationDetail.getConfirmedSatisfactoryTimestamp(),
              pwaApplicationDetail.getConfirmedSatisfactoryByPersonId())
              .setPersonLabelText("Confirmed by")
              .setPersonEmailLabel("Contact email");

          if (!StringUtils.isBlank(pwaApplicationDetail.getConfirmedSatisfactoryReason())) {
            builder.addDataItem("Reasons for confirmation", pwaApplicationDetail.getConfirmedSatisfactoryReason());
          }

          return builder.build();

        })
        .collect(Collectors.toList());

  }

}
