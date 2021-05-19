package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class ApplicationSubmissionCaseHistoryItemService implements CaseHistoryItemService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public ApplicationSubmissionCaseHistoryItemService(PwaApplicationDetailService pwaApplicationDetailService,
                                                     ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {

    var appDetails = pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(pwaApplication)
        .stream()
        .sorted(Comparator.comparing(PwaApplicationDetail::getVersionNo))
        .collect(Collectors.toList());

    var updateRequestAppDetailMap = applicationUpdateRequestService.getApplicationUpdateRequests(appDetails)
        .stream()
        .collect(Collectors.toMap(ApplicationUpdateRequest::getPwaApplicationDetail, Function.identity()));;

    return appDetails.stream()
        .map(pwaApplicationDetail -> {
          var builder = new CaseHistoryItemView.Builder(
              String.format("Application version %s submitted", pwaApplicationDetail.getVersionNo()),
              pwaApplicationDetail.getSubmittedTimestamp(),
              pwaApplicationDetail.getSubmittedByPersonId()
          )
              .setPersonLabelText("Submitted by")
              .setPersonEmailLabel("Contact email");

          if (updateRequestAppDetailMap.containsKey(pwaApplicationDetail)) {
            builder.addDataItem("Update request deadline",
                DateUtils.formatDate(updateRequestAppDetailMap.get(pwaApplicationDetail).getDeadlineTimestamp()));
          }

          return builder.build();
        })
        .collect(Collectors.toList());
  }

}
