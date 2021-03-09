package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class ApplicationChargeRequestHistoryItemService implements CaseHistoryItemService {

  static final String STATUS_LABEL = "Request status";
  static final String DESCRIPTION_LABEL = "Description";
  static final String FORMATTED_TOTAL_LABEL = "Total";

  static final String PAID_BY_LABEL = "Paid by";
  static final String PAID_BY_EMAIL = "Paid by contact email";
  static final String PAID_AT_LABEL = "Paid on";

  static final String CANCELLED_BY_LABEL = "Cancelled by";
  static final String CANCELLED_BY_EMAIL = "Cancelled by contact email";
  static final String CANCELLED_AT_LABEL = "Cancelled on";
  static final String CANCELLED_REASON_LABEL = "Cancelled reason";

  private static final String WAIVED_REASON_LABEL = "Waived reason";

  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final ApplicationPaymentSummariser applicationPaymentSummariser;
  private final PersonService personService;

  @Autowired
  public ApplicationChargeRequestHistoryItemService(ApplicationChargeRequestService applicationChargeRequestService,
                                                    ApplicationPaymentSummariser applicationPaymentSummariser,
                                                    PersonService personService) {
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationPaymentSummariser = applicationPaymentSummariser;
    this.personService = personService;
  }


  @Override
  @Transactional(readOnly = true)
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {
    var allAppChargeReports = applicationChargeRequestService
        .getAllApplicationChargeRequestReportsForApplication(pwaApplication);

    return allAppChargeReports.stream()
        .map(applicationChargeRequestReport -> {
              var builder = new CaseHistoryItemView.Builder(
                  "Application payment request",
                  applicationChargeRequestReport.getRequestedInstant(),
                  applicationChargeRequestReport.getRequestedByPersonId())
                  .setPersonLabelText("Requested by")
                  .setPersonEmailLabel("Contact email");

              setCaseHistoryItemDataItems(builder, applicationChargeRequestReport);
              return builder.build();
            }
        )
        .collect(Collectors.toList());
  }

  private void setCaseHistoryItemDataItems(CaseHistoryItemView.Builder caseHistoryItemBuilder,
                                           ApplicationChargeRequestReport applicationChargeRequestReport) {

    var summarisedReport = applicationPaymentSummariser.summarise(applicationChargeRequestReport);

    caseHistoryItemBuilder.addDataItem(
        STATUS_LABEL,
        applicationChargeRequestReport.getPwaAppChargeRequestStatus().getDispayString());
    caseHistoryItemBuilder.addDataItem(DESCRIPTION_LABEL, summarisedReport.getHeadlineSummary());
    caseHistoryItemBuilder.addDataItem(FORMATTED_TOTAL_LABEL, summarisedReport.getFormattedAmount());

    switch (applicationChargeRequestReport.getPwaAppChargeRequestStatus()) {
      case WAIVED:
        caseHistoryItemBuilder.addDataItem(WAIVED_REASON_LABEL, applicationChargeRequestReport.getWaivedReason());
        break;
      case PAID:
        var paidByPerson = personService.getPersonById(applicationChargeRequestReport.getPaidByPersonId());
        caseHistoryItemBuilder.addDataItem(
            PAID_AT_LABEL,
            DateUtils.formatDateTime(applicationChargeRequestReport.getPaidInstant()));
        caseHistoryItemBuilder.addDataItem(PAID_BY_LABEL, paidByPerson.getFullName());
        caseHistoryItemBuilder.addDataItem(PAID_BY_EMAIL, paidByPerson.getEmailAddress());
        break;
      case CANCELLED:
        var cancelledByPerson = personService.getPersonById(applicationChargeRequestReport.getLastUpdatedPersonId());
        caseHistoryItemBuilder.addDataItem(
            CANCELLED_AT_LABEL,
            DateUtils.formatDateTime(applicationChargeRequestReport.getLastUpdatedInstant()));
        caseHistoryItemBuilder.addDataItem(CANCELLED_BY_LABEL, cancelledByPerson.getFullName());
        caseHistoryItemBuilder.addDataItem(CANCELLED_BY_EMAIL, cancelledByPerson.getEmailAddress());
        caseHistoryItemBuilder.addDataItem(CANCELLED_REASON_LABEL, applicationChargeRequestReport.getCancelledReason());
        break;
      default:
        // do nothing, other statuses dont need e
    }

  }

}
