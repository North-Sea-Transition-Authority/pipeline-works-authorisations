package uk.co.ogauthority.pwa.service.appprocessing.casehistory;


import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ConfirmSatisfactoryApplicationCaseHistoryItemServiceTest {

  private final static PersonId PERSON_ID1 = new PersonId(1);
  private final static PersonId PERSON_ID2 = new PersonId(2);

  private final static int APP_ID = 10;
  private final static int APP_DETAIL_1_ID = 1;
  private final static int APP_DETAIL_2_ID = 2;

  private final static Instant APP_DETAIL_1_CONFIRMED = Instant.now().truncatedTo(ChronoUnit.DAYS);
  private final static Instant APP_DETAIL_2_CONFIRMED = Instant.now()
      .truncatedTo(ChronoUnit.DAYS)
      .plus(1, ChronoUnit.HOURS);

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private ConfirmSatisfactoryApplicationCaseHistoryItemService confirmSatisfactoryApplicationCaseHistoryItemService;

  private PwaApplicationDetail detail1;
  private PwaApplicationDetail detail2;
  private PwaApplicationDetail ignoredDetail;
  private PwaApplication application;


  @BeforeEach
  void setUp() throws Exception {

    confirmSatisfactoryApplicationCaseHistoryItemService = new ConfirmSatisfactoryApplicationCaseHistoryItemService(pwaApplicationDetailService);

    detail1 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, APP_DETAIL_1_ID, 1);
    detail1.setConfirmedSatisfactoryTimestamp(APP_DETAIL_1_CONFIRMED);
    detail1.setConfirmedSatisfactoryByPersonId(PERSON_ID1);

    detail2 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, APP_DETAIL_2_ID, 2);
    detail2.setConfirmedSatisfactoryTimestamp(APP_DETAIL_2_CONFIRMED);
    detail2.setConfirmedSatisfactoryByPersonId(PERSON_ID2);
    detail2.setConfirmedSatisfactoryReason("reason");

    ignoredDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, 5, 3);

    application = detail1.getPwaApplication();
    detail2.setPwaApplication(application);
    ignoredDetail.setPwaApplication(application);

  }

  @Test
  void getCaseHistoryItemViews_whenMultipleVersionsConfirmed_andNotAllVersionsConfirmed() {

    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(application))
        .thenReturn(List.of(detail1, detail2, ignoredDetail));

    var historyItems = confirmSatisfactoryApplicationCaseHistoryItemService.getCaseHistoryItemViews(application);

    assertThat(historyItems).containsExactly(
        new CaseHistoryItemView.Builder("Application version 1 confirmed satisfactory", APP_DETAIL_1_CONFIRMED, PERSON_ID1)
            .setPersonLabelText("Confirmed by")
            .setPersonEmailLabel("Contact email")
            .build(),
        new CaseHistoryItemView.Builder("Application version 2 confirmed satisfactory", APP_DETAIL_2_CONFIRMED, PERSON_ID2)
            .setPersonLabelText("Confirmed by")
            .setPersonEmailLabel("Contact email")
            .addDataItem("Reasons for confirmation", detail2.getConfirmedSatisfactoryReason())
            .build()
    );

  }
}