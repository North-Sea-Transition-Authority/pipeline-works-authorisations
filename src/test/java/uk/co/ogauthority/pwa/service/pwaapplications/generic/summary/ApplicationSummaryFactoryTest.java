package uk.co.ogauthority.pwa.service.pwaapplications.generic.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSummaryFactoryTest {

  private static final String FORENAME = "FORENAME";
  private static final String SURNAME = "SURNAME";
  private static final PersonId SUBMITTER_PERSON_ID = new PersonId(10);

  @Mock
  private PersonService personService;

  private ApplicationSummaryFactory applicationSummaryFactory;

  private Person person;

  private PwaApplicationDetail detail;

  private LocalDateTime submitDateTime;
  private Instant submitInstant;

  @Before
  public void setup() {
    person = new Person(SUBMITTER_PERSON_ID.asInt(), FORENAME, SURNAME, "email", "tel");
    applicationSummaryFactory = new ApplicationSummaryFactory(personService);

    when(personService.getPersonById(SUBMITTER_PERSON_ID)).thenReturn(person);

    submitDateTime = LocalDateTime.of(2020, Month.JANUARY, 1, 11, 59, 0);
    submitInstant = submitDateTime.toInstant(ZoneOffset.UTC);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setSubmittedTimestamp(submitInstant);
    detail.setSubmittedByPersonId(SUBMITTER_PERSON_ID);


  }


  @Test
  public void createSubmissionSummary_verifyServiceInteractions_() {
    var summary = applicationSummaryFactory.createSubmissionSummary(detail);
    verify(personService, times(1)).getPersonById(SUBMITTER_PERSON_ID);

  }

  @Test
  public void createSubmissionSummary_summaryCreatedAsExpected() {
    var summary = applicationSummaryFactory.createSubmissionSummary(detail);
    assertThat(summary.getApplicationReference()).isEqualTo(detail.getPwaApplicationRef());
    assertThat(summary.getIsFirstVersion()).isEqualTo(true);
    assertThat(summary.getSubmissionDateTime()).isEqualTo(submitDateTime);
    assertThat(summary.getSubmittedBy()).isEqualTo(FORENAME + " " + SURNAME);

  }
}