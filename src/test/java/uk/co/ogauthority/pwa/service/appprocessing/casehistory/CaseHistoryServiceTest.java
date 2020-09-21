package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.repository.PersonRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.casenotes.CaseNoteService;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
public class CaseHistoryServiceTest {

  @MockBean
  private CaseNoteService caseNoteService;

  @MockBean
  private PersonRepository personRepository;

  @Autowired
  private CaseHistoryService caseHistoryService;

  private CaseNote caseNote1, caseNote2;

  @Test
  public void getCaseHistory() {

    var app = new PwaApplication();
    var person1 = new Person(1, "fore", "sur", null, null);
    var person2 = new Person(2, "fore2", "sur2", null, null);

    caseNote1 = new CaseNote(new PwaApplication(), person1.getId(), Instant.now(), "note1");
    caseNote2 = new CaseNote(new PwaApplication(), person2.getId(), Instant.now().minusSeconds(100), "note2");

    var fileView = new UploadedFileView("id", "name", 1L, "desc", Instant.now(), "#");

    when(caseNoteService.getCaseHistoryItemViews(any())).thenReturn(List.of(
        CaseHistoryItemViewFactory.create(caseNote1, List.of(fileView)),
        CaseHistoryItemViewFactory.create(caseNote2, List.of())
    ));

    when(personRepository.findAllByIdIn(any())).thenReturn(List.of(person1, person2));

    var caseHistoryItemViews = caseHistoryService.getCaseHistory(app);

    verify(caseNoteService, times(1)).getCaseHistoryItemViews(app);

    verify(personRepository, times(1)).findAllByIdIn(Set.of(1, 2));

    assertThat(caseHistoryItemViews)
        .extracting(
            CaseHistoryItemView::getDateTime,
            CaseHistoryItemView::getDateTimeDisplay,
            CaseHistoryItemView::getHeaderText,
            CaseHistoryItemView::getPersonId,
            CaseHistoryItemView::getPersonLabelText,
            CaseHistoryItemView::getPersonName,
            CaseHistoryItemView::getDataItems,
            CaseHistoryItemView::getUploadedFileViews)
        .containsExactly(
            tuple(caseNote1.getDateTime(), DateUtils.formatDateTime(caseNote1.getDateTime()), "Case note", caseNote1.getPersonId(), "Created by", person1.getFullName(), Map.of("Note text", caseNote1.getNoteText()), List.of(fileView)),
            tuple(caseNote2.getDateTime(), DateUtils.formatDateTime(caseNote2.getDateTime()), "Case note", caseNote2.getPersonId(), "Created by", person2.getFullName(), Map.of("Note text", caseNote2.getNoteText()), List.of())
        );



  }

}
