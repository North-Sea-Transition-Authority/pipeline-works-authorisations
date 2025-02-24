package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReview;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.DataItemRow;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class ConsentReviewCaseHistoryItemServiceTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private ConsentReviewService consentReviewService;

  @Mock
  private PersonService personService;

  private Clock clock;

  private ConsentReviewCaseHistoryItemService consentReviewCaseHistoryItemService;

  private final PwaApplication pwaApplication = new PwaApplication();
  private PwaApplicationDetail detail, detail2;

  private Person pwaManagerPerson = PersonTestUtil.createDefaultPerson();

  @BeforeEach
  void setUp() throws Exception {

    clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    consentReviewCaseHistoryItemService = new ConsentReviewCaseHistoryItemService(pwaApplicationDetailService, consentReviewService, personService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setPwaApplication(pwaApplication);
    detail2 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail2.setPwaApplication(pwaApplication);
    detail2.setVersionNo(2);

    when(pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication)).thenReturn(List.of(detail, detail2));

    when(personService.findAllByIdIn(any())).thenReturn(List.of(pwaManagerPerson));

  }

  @Test
  void getCaseHistoryItemViews() {

    var openReview = new ConsentReview(detail, "cover 1", new PersonId(1), clock.instant());
    var returnedReview = new ConsentReview(detail2, "cover 2", new PersonId(2), clock.instant().plus(1, ChronoUnit.DAYS));
    returnedReview.setStatus(ConsentReviewStatus.RETURNED);
    returnedReview.setEndedReason("end reason");
    returnedReview.setEndedByPersonId(pwaManagerPerson.getId());
    returnedReview.setEndTimestamp(clock.instant().plus(5, ChronoUnit.DAYS));

    var approvedReview = new ConsentReview(detail2, "cover 3", new PersonId(2), clock.instant().plus(2, ChronoUnit.DAYS));
    approvedReview.setStatus(ConsentReviewStatus.APPROVED);
    approvedReview.setEndedByPersonId(pwaManagerPerson.getId());
    approvedReview.setEndTimestamp(clock.instant().plus(7, ChronoUnit.DAYS));

    when(consentReviewService.findByPwaApplicationDetails(List.of(detail, detail2))).thenReturn(List.of(openReview, returnedReview, approvedReview));

    var historyItems = consentReviewCaseHistoryItemService.getCaseHistoryItemViews(pwaApplication);

    assertThat(historyItems.get(0)).satisfies(item -> {
      assertThat(item.getDateTime()).isEqualTo(openReview.getStartTimestamp());
      assertThat(item.getHeaderText()).isEqualTo("Consent review");
      assertThat(item.getPersonId()).isEqualTo(openReview.getStartedByPersonId());
    });

    assertThat(historyItems.get(1)).satisfies(item -> {
      assertThat(item.getDateTime()).isEqualTo(returnedReview.getStartTimestamp());
      assertThat(item.getHeaderText()).isEqualTo("Consent review");
      assertThat(item.getPersonId()).isEqualTo(returnedReview.getStartedByPersonId());
      assertThat(item.getDataItemRows()).containsExactly(
          new DataItemRow(Map.of(
              "Returned on", DateUtils.formatDateTime(returnedReview.getEndTimestamp()),
                  "Returned by", pwaManagerPerson.getFullName(),
                  "Contact email", pwaManagerPerson.getEmailAddress())),
          new DataItemRow(Map.of("Return reason", returnedReview.getEndedReason())));
    });

    assertThat(historyItems.get(2)).satisfies(item -> {
      assertThat(item.getDateTime()).isEqualTo(approvedReview.getStartTimestamp());
      assertThat(item.getHeaderText()).isEqualTo("Consent review");
      assertThat(item.getPersonId()).isEqualTo(approvedReview.getStartedByPersonId());
      assertThat(item.getDataItemRows()).containsExactly(
          new DataItemRow(Map.of(
              "Approved on", DateUtils.formatDateTime(approvedReview.getEndTimestamp()),
              "Approved by", pwaManagerPerson.getFullName(),
              "Contact email", pwaManagerPerson.getEmailAddress())));
    });

  }


  @Test
  void getCaseHistoryItemViews_consentReviewNotEnded_historyItemViewStillCreated() {

    var openReview = new ConsentReview(detail, "cover 1", new PersonId(1), clock.instant());
    openReview.setEndedByPersonId(null);

    when(consentReviewService.findByPwaApplicationDetails(List.of(detail, detail2))).thenReturn(List.of(openReview));

    consentReviewCaseHistoryItemService.getCaseHistoryItemViews(pwaApplication);

    verify(personService).findAllByIdIn(Set.of());
  }

}