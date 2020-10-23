package uk.co.ogauthority.pwa.service.appprocessing.casehistory;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSubmissionCaseHistoryItemServiceTest {

  private final static PersonId PERSON_ID1 = new PersonId(1);
  private final static PersonId PERSON_ID2 = new PersonId(2);

  private final static int APP_ID = 10;
  private final static int APP_DETAIL_1_ID = 1;
  private final static int APP_DETAIL_2_ID = 2;

  private final static Instant APP_DETAIL_1_SUBMITTED = Instant.now().truncatedTo(ChronoUnit.DAYS);
  private final static Instant APP_DETAIL_2_SUBMITTED = Instant.now()
      .truncatedTo(ChronoUnit.DAYS)
      .plus(1, ChronoUnit.HOURS);

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private ApplicationSubmissionCaseHistoryItemService applicationSubmissionCaseHistoryItemService;

  private PwaApplicationDetail detail1;
  private PwaApplicationDetail detail2;
  private PwaApplication application;


  @Before
  public void setUp() throws Exception {

    applicationSubmissionCaseHistoryItemService = new ApplicationSubmissionCaseHistoryItemService(
        pwaApplicationDetailService
    );

    detail1 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, APP_DETAIL_1_ID,
        1);
    detail1.setSubmittedTimestamp(APP_DETAIL_1_SUBMITTED);
    detail1.setSubmittedByPersonId(PERSON_ID1);
    detail2 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID, APP_DETAIL_2_ID,
        2);
    detail2.setSubmittedTimestamp(APP_DETAIL_2_SUBMITTED);
    detail2.setSubmittedByPersonId(PERSON_ID2);
    application = detail1.getPwaApplication();

    detail2.setPwaApplication(application);
  }

  @Test
  public void getCaseHistoryItemViews_whenMultipleVersions() {
    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(application))
        .thenReturn(List.of(detail1, detail2));

    var historyItems = applicationSubmissionCaseHistoryItemService.getCaseHistoryItemViews(application);

    assertThat(historyItems).containsExactly(
        new CaseHistoryItemView.Builder("Application version 1 submitted", APP_DETAIL_1_SUBMITTED, PERSON_ID1)
            .setPersonLabelText("Submitted by")
            .setPersonEmailLabel("Contact email")
            .build(),
        new CaseHistoryItemView.Builder("Application version 2 submitted", APP_DETAIL_2_SUBMITTED, PERSON_ID2)
            .setPersonLabelText("Submitted by")
            .setPersonEmailLabel("Contact email")
            .build()
    );

  }
}