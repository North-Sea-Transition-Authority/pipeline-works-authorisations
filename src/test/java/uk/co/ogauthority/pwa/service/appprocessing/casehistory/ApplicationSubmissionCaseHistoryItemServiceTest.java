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
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionCaseHistoryItemServiceTest {

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

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  private ApplicationSubmissionCaseHistoryItemService applicationSubmissionCaseHistoryItemService;

  private PwaApplicationDetail detail1;
  private PwaApplicationDetail detail2;
  private PwaApplication application;


  @BeforeEach
  void setUp() throws Exception {

    applicationSubmissionCaseHistoryItemService = new ApplicationSubmissionCaseHistoryItemService(
        pwaApplicationDetailService, applicationUpdateRequestService);

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
  void getCaseHistoryItemViews_whenMultipleVersions() {

    var details = List.of(detail1, detail2);
    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(application))
        .thenReturn(details);

    var detail1AppUpdateRequest = new ApplicationUpdateRequest();
    detail1AppUpdateRequest.setPwaApplicationDetail(detail1);
    detail1AppUpdateRequest.setDeadlineTimestamp(Instant.now());
    var detail2AppUpdateRequest = new ApplicationUpdateRequest();
    detail2AppUpdateRequest.setPwaApplicationDetail(detail2);
    detail2AppUpdateRequest.setDeadlineTimestamp(Instant.now().plus(1, ChronoUnit.DAYS));

    when(applicationUpdateRequestService.getApplicationUpdateRequests(details))
        .thenReturn(List.of(detail1AppUpdateRequest, detail2AppUpdateRequest));

    var historyItems = applicationSubmissionCaseHistoryItemService.getCaseHistoryItemViews(application);

    assertThat(historyItems).containsExactly(
        new CaseHistoryItemView.Builder("Application version 1 submitted", APP_DETAIL_1_SUBMITTED, PERSON_ID1)
            .setPersonLabelText("Submitted by")
            .setPersonEmailLabel("Contact email")
            .addDataItem("Update request deadline",
                DateUtils.formatDate(detail1AppUpdateRequest.getDeadlineTimestamp()))
            .build(),
        new CaseHistoryItemView.Builder("Application version 2 submitted", APP_DETAIL_2_SUBMITTED, PERSON_ID2)
            .setPersonLabelText("Submitted by")
            .setPersonEmailLabel("Contact email")
            .addDataItem("Update request deadline",
                DateUtils.formatDate(detail2AppUpdateRequest.getDeadlineTimestamp()))
            .build()
    );

  }

  @Test
  void getCaseHistoryItemViews_only1VersionExists_noUpdateRequest() {

    when(pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(application))
        .thenReturn(List.of(detail1));

    var historyItems = applicationSubmissionCaseHistoryItemService.getCaseHistoryItemViews(application);

    assertThat(historyItems).containsExactly(
        new CaseHistoryItemView.Builder("Application version 1 submitted", APP_DETAIL_1_SUBMITTED, PERSON_ID1)
            .setPersonLabelText("Submitted by")
            .setPersonEmailLabel("Contact email")
            .build()
    );

  }

}