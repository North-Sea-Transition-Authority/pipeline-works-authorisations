package uk.co.ogauthority.pwa.service.appprocessing.casehistory;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.DataItemRow;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReportTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentDisplaySummaryTestUtil;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationChargeRequestHistoryItemServiceTest {

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


  @Before
  public void setUp() throws Exception {
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
    when(personService.getPersonById(any())).thenReturn(fakePerson);
  }

  @Test
  public void getCaseHistoryItemViews_whenRequestOpen() {
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
  public void getCaseHistoryItemViews_whenRequestCancelled() {
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
  public void getCaseHistoryItemViews_whenRequestWaived() {
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

    verify(personService, times(0)).getPersonById(any());
  }

  @Test
  public void getCaseHistoryItemViews_whenRequestPaid() {
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