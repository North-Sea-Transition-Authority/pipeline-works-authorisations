package uk.co.ogauthority.pwa.service.appprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.enums.appprocessing.casehistory.CaseHistoryItemType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceTest {

  private CaseNoteService caseNoteService;

  @Mock
  private CaseNoteRepository caseNoteRepository;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Captor
  private ArgumentCaptor<CaseNote> caseNoteCaptor;

  @Before
  public void setUp() {
    caseNoteService = new CaseNoteService(caseNoteRepository, clock);
  }

  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.ADD_CASE_NOTE), null);

    boolean canShow = caseNoteService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null);

    boolean canShow = caseNoteService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void createCaseNote() {

    var app = new PwaApplication();
    var person = new Person(1, null, null, null, null);
    var noteText = "some test text";

    caseNoteService.createCaseNote(app, noteText, person);

    verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture());

    var caseNote = caseNoteCaptor.getValue();

    assertThat(caseNote.getPwaApplication()).isEqualTo(app);
    assertThat(caseNote.getPersonId()).isEqualTo(person.getId());
    assertThat(caseNote.getDateTime()).isEqualTo(clock.instant());
    assertThat(caseNote.getItemType()).isEqualTo(CaseHistoryItemType.CASE_NOTE);
    assertThat(caseNote.getNoteText()).isEqualTo(noteText);

  }

  @Test
  public void getCaseHistoryItemViews() {

    var app = new PwaApplication();

    when(caseNoteRepository.getAllByPwaApplication(any())).thenReturn(List.of(
        new CaseNote(app, new PersonId(1), clock.instant(), "noteText"),
        new CaseNote(app, new PersonId(2), clock.instant().minusSeconds(10), "note2")
    ));

    var views = caseNoteService.getCaseHistoryItemViews(app);

    assertThat(views)
        .extracting(
            CaseHistoryItemView::getDateTime,
            CaseHistoryItemView::getDateTimeDisplay,
            CaseHistoryItemView::getHeaderText,
            CaseHistoryItemView::getPersonId,
            CaseHistoryItemView::getPersonLabelText,
            CaseHistoryItemView::getPersonName,
            CaseHistoryItemView::getDataItems)
        .contains(
            tuple(clock.instant(), DateUtils.formatDateTime(clock.instant()), "Case note", new PersonId(1), "Created by", null, Map.of("Note text", "noteText")),
            tuple(clock.instant().minusSeconds(10), DateUtils.formatDateTime(clock.instant().minusSeconds(10)), "Case note", new PersonId(2), "Created by", null, Map.of("Note text", "note2"))
        );

  }

}
