package uk.co.ogauthority.pwa.service.appprocessing.casehistory;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReportTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummaryTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.DataItemRow;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class ApplicationChargeRequestHistoryItemServiceTest {

  private static final String FORMATTED_AMOUNT = "1.00";
  private static final int PENNIES = 100;
  private static final String SUMMARY = "SUMMARY";

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  @Mock
  private ApplicationPaymentSummariser applicationPaymentSummariser;

  @Mock
  private PersonService personService;


  private PwaApplication pwaApplication;
  private Person fakePerson;
  private PersonId requesterPersonId;
  private PersonId cancelledByPersonId;
  private PersonId waivedByPersonId;
  private PersonId paidByPersonId;

  private Instant requestInstant;


  private ApplicationChargeRequestHistoryItemService caseHistoryService;


  @BeforeEach
  void setUp() throws Exception {
    requesterPersonId = new PersonId(1);
    cancelledByPersonId = new PersonId(2);
    waivedByPersonId = new PersonId(3);
    paidByPersonId = new PersonId(4);

    requestInstant = Instant.now();

    caseHistoryService = new ApplicationChargeRequestHistoryItemService(
        applicationChargeRequestService,
        applicationPaymentSummariser,
        personService
    );

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();

    fakePerson = PersonTestUtil.createDefaultPerson();
  }

  @Test
  void getCaseHistoryItemViews_whenRequestOpen() {
    var applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createOpenReport(
        PENNIES,
        SUMMARY,
        requestInstant,
        requesterPersonId,
        List.of()
    );
    when(applicationChargeRequestService.getAllApplicationChargeRequestReportsForApplication(any()))
        .thenReturn(List.of(applicationChargeRequestReport));
    var summarisedReport = ApplicationPaymentDisplaySummaryTestUtil.createSimpleSummary(SUMMARY, FORMATTED_AMOUNT);
    when(applicationPaymentSummariser.summarise(applicationChargeRequestReport)).thenReturn(summarisedReport);

    var caseHistoryItems = caseHistoryService.getCaseHistoryItemViews(pwaApplication);

    assertThat(caseHistoryItems).hasOnlyOneElementSatisfying(caseHistoryItemView -> {
      assertThat(caseHistoryItemView.getDateTime()).isEqualTo(requestInstant);
      assertThat(caseHistoryItemView.getPersonId()).isEqualTo(requesterPersonId);
      assertThat(caseHistoryItemView.getHeaderText()).isNotNull();

      assertStandardDataItems(caseHistoryItemView.getDataItemRows(), PwaAppChargeRequestStatus.OPEN);
      assertThat(caseHistoryItemView.getDataItemRows()).hasSize(1);

    });
  }

  @Test
  void getCaseHistoryItemViews_whenRequestCancelled() {
    when(personService.getPersonById(any(PersonId.class))).thenReturn(fakePerson);

    var cancelledInstant = requestInstant.plus(1, ChronoUnit.DAYS);
    var applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createCancelledReport(
        PENNIES,
        SUMMARY,
        requestInstant,
        requesterPersonId,
        cancelledInstant,
        cancelledByPersonId
    );
    when(applicationChargeRequestService.getAllApplicationChargeRequestReportsForApplication(any()))
        .thenReturn(List.of(applicationChargeRequestReport));
    var summarisedReport = ApplicationPaymentDisplaySummaryTestUtil.createSimpleSummary(SUMMARY, FORMATTED_AMOUNT);
    when(applicationPaymentSummariser.summarise(applicationChargeRequestReport)).thenReturn(summarisedReport);

    var caseHistoryItems = caseHistoryService.getCaseHistoryItemViews(pwaApplication);

    assertThat(caseHistoryItems).hasOnlyOneElementSatisfying(caseHistoryItemView -> {
      assertThat(caseHistoryItemView.getDateTime()).isEqualTo(requestInstant);
      assertThat(caseHistoryItemView.getPersonId()).isEqualTo(requesterPersonId);
      assertThat(caseHistoryItemView.getHeaderText()).isNotNull();

      assertStandardDataItems(caseHistoryItemView.getDataItemRows(), PwaAppChargeRequestStatus.CANCELLED);
      assertThat(caseHistoryItemView.getDataItemRows()).hasSize(3);

      assertThat(caseHistoryItemView.getDataItemRows().get(1).getDataItems())
          .hasSize(3)
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.CANCELLED_AT_LABEL,
              s -> assertThat(s).isEqualTo(DateUtils.formatDateTime(cancelledInstant)))
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.CANCELLED_BY_EMAIL,
              s -> assertThat(s).isEqualTo(fakePerson.getEmailAddress()))
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.CANCELLED_BY_LABEL,
              s -> assertThat(s).isEqualTo(fakePerson.getFullName()));

      assertThat(caseHistoryItemView.getDataItemRows().get(2).getDataItems())
          .hasSize(1)
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.CANCELLED_REASON_LABEL,
              s -> assertThat(s).isEqualTo(applicationChargeRequestReport.getCancelledReason()));

      verify(personService, times(1)).getPersonById(cancelledByPersonId);
    });
  }

  @Test
  void getCaseHistoryItemViews_whenRequestWaived() {
    var cancelledInstant = requestInstant.plus(1, ChronoUnit.DAYS);
    var applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createWaivedReport(
        PENNIES,
        SUMMARY,
        requestInstant,
        requesterPersonId
    );
    when(applicationChargeRequestService.getAllApplicationChargeRequestReportsForApplication(any()))
        .thenReturn(List.of(applicationChargeRequestReport));
    var summarisedReport = ApplicationPaymentDisplaySummaryTestUtil.createSimpleSummary(SUMMARY, FORMATTED_AMOUNT);
    when(applicationPaymentSummariser.summarise(applicationChargeRequestReport)).thenReturn(summarisedReport);

    var caseHistoryItems = caseHistoryService.getCaseHistoryItemViews(pwaApplication);

    assertThat(caseHistoryItems).hasOnlyOneElementSatisfying(caseHistoryItemView -> {
      assertThat(caseHistoryItemView.getDateTime()).isEqualTo(requestInstant);
      assertThat(caseHistoryItemView.getPersonId()).isEqualTo(requesterPersonId);
      assertThat(caseHistoryItemView.getHeaderText()).isNotNull();

      assertStandardDataItems(caseHistoryItemView.getDataItemRows(), PwaAppChargeRequestStatus.WAIVED);
      assertThat(caseHistoryItemView.getDataItemRows()).hasSize(2);

      assertThat(caseHistoryItemView.getDataItemRows().get(1).getDataItems())
          .hasSize(1)
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.WAIVED_REASON_LABEL,
              s -> assertThat(s).isEqualTo(applicationChargeRequestReport.getWaivedReason()));
    });

    verify(personService, times(0)).getPersonById(any(PersonId.class));
  }

  @Test
  void getCaseHistoryItemViews_whenRequestPaid() {
    when(personService.getPersonById(any(PersonId.class))).thenReturn(fakePerson);
    
    var paidInstant = requestInstant.plus(1, ChronoUnit.DAYS);
    var applicationChargeRequestReport = ApplicationChargeRequestReportTestUtil.createPaidReport(
        PENNIES,
        SUMMARY,
        requestInstant,
        requesterPersonId,
        paidInstant,
        paidByPersonId
    );
    when(applicationChargeRequestService.getAllApplicationChargeRequestReportsForApplication(any()))
        .thenReturn(List.of(applicationChargeRequestReport));
    var summarisedReport = ApplicationPaymentDisplaySummaryTestUtil.createSimpleSummary(SUMMARY, FORMATTED_AMOUNT);
    when(applicationPaymentSummariser.summarise(applicationChargeRequestReport)).thenReturn(summarisedReport);

    var caseHistoryItems = caseHistoryService.getCaseHistoryItemViews(pwaApplication);

    assertThat(caseHistoryItems).hasOnlyOneElementSatisfying(caseHistoryItemView -> {
      assertThat(caseHistoryItemView.getDateTime()).isEqualTo(requestInstant);
      assertThat(caseHistoryItemView.getPersonId()).isEqualTo(requesterPersonId);
      assertThat(caseHistoryItemView.getHeaderText()).isNotNull();

      assertStandardDataItems(caseHistoryItemView.getDataItemRows(), PwaAppChargeRequestStatus.PAID);

      assertThat(caseHistoryItemView.getDataItemRows().get(1).getDataItems())
          .hasSize(3)
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.PAID_AT_LABEL,
              s -> assertThat(s).isEqualTo(DateUtils.formatDateTime(paidInstant)))
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.PAID_BY_EMAIL,
              s -> assertThat(s).isEqualTo(fakePerson.getEmailAddress()))
          .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.PAID_BY_LABEL,
              s -> assertThat(s).isEqualTo(fakePerson.getFullName()));
    });

    verify(personService, times(1)).getPersonById(paidByPersonId);
  }

  private void assertStandardDataItems(List<DataItemRow> dataItemRows, PwaAppChargeRequestStatus status) {
    assertThat(dataItemRows.get(0).getDataItems())
        .hasSize(3)
        .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.STATUS_LABEL,
            s -> assertThat(s).isEqualTo(status.getDispayString()))
        .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.DESCRIPTION_LABEL,
            s -> assertThat(s).isEqualTo(SUMMARY))
        .hasEntrySatisfying(ApplicationChargeRequestHistoryItemService.FORMATTED_TOTAL_LABEL,
            s -> assertThat(s).isEqualTo(FORMATTED_AMOUNT));
  }
}