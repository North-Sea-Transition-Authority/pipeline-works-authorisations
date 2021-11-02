package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.Before;
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
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.casenotes.CaseNoteService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
public class CaseHistoryServiceTest {

  public static String HISTORY_ITEM_HEADER = "HEADER";
  public static String NOTE_LABEL = "Note label";
  public static String NOTE_WITH_FILES_DOWNLOAD_URL = "www.some.url.com";
  public static String NOTE_WITH_FILES_TEXT = "note with Files";
  public static String NOTE_NO_FILES_TEXT = "note no Files";

  @MockBean
  private CaseNoteService caseNoteService;

  @MockBean
  private PersonService personService;

  @Autowired
  private CaseHistoryService caseHistoryService;

  @MockBean
  private ApplicationSubmissionCaseHistoryItemService applicationSubmissionCaseHistoryItemService;

  private CaseHistoryItemView firstCaseHistory, secondCaseHistoryWithFiles;

  private PwaApplication pwaApplication;

  private Instant firstCaseHistoryInstant;
  private Instant secondCaseHistoryInstant;

  private Person person1;
  private Person person2;

  @Before
  public void setup(){
    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL)
        .getPwaApplication();

    person1 = new Person(1, "fore", "sur", null, null);
    person2 = new Person(2, "fore2", "sur2", null, null);

    var fileView = new UploadedFileView("id", "name", 1L, "desc", Instant.now(), "#");

    firstCaseHistoryInstant = Instant.now();
    secondCaseHistoryInstant = Instant.now().minusSeconds(100);

    firstCaseHistory = new CaseHistoryItemView.Builder(HISTORY_ITEM_HEADER, firstCaseHistoryInstant, person1.getId())
        .addDataItem(NOTE_LABEL, NOTE_NO_FILES_TEXT)
        .build();

    secondCaseHistoryWithFiles = new CaseHistoryItemView.Builder(
        HISTORY_ITEM_HEADER,
        secondCaseHistoryInstant,
        person2.getId()
    )
        .setUploadedFileViews(List.of(fileView), NOTE_WITH_FILES_DOWNLOAD_URL)
        .addDataItem(NOTE_LABEL, NOTE_WITH_FILES_TEXT)
        .build();

    when(caseNoteService.getCaseHistoryItemViews(any())).thenReturn(List.of(
        secondCaseHistoryWithFiles
    ));

    when(applicationSubmissionCaseHistoryItemService.getCaseHistoryItemViews(any())).thenReturn(List.of(
        firstCaseHistory
    ));

    when(personService.findAllByIdIn(any())).thenReturn(List.of(person1, person2));
  }

  @Test
  public void getCaseHistory_whenHistoryItemsReturned_verifyServiceInteractions() {

    var caseHistoryItemViews = caseHistoryService.getCaseHistory(pwaApplication);

    verify(caseNoteService, times(1)).getCaseHistoryItemViews(pwaApplication);
    verify(applicationSubmissionCaseHistoryItemService, times(1)).getCaseHistoryItemViews(pwaApplication);

    verify(personService, times(1)).findAllByIdIn(Set.of(person1.getId(), person2.getId()));

  }

  @Test
  public void getCaseHistory_whenHistoryItemsReturned_thenItemIndexesAreInOrder() {

    when(caseNoteService.getCaseHistoryItemViews(any())).thenReturn(List.of(
        firstCaseHistory,
        secondCaseHistoryWithFiles
    ));
    when(applicationSubmissionCaseHistoryItemService.getCaseHistoryItemViews(any())).thenReturn(List.of());

    var caseHistoryItemViews = caseHistoryService.getCaseHistory(pwaApplication);

    assertThat(caseHistoryItemViews)
        .extracting(CaseHistoryItemView::getDisplayIndex)
        .containsExactly(1, 2);
  }

  @Test
  public void getCaseHistory_whenHistoryItemsReturned_thenPersonNameIsSet_andNotesInOrderOfDate() {

    var caseHistoryItemViews = caseHistoryService.getCaseHistory(pwaApplication);

    assertThat(caseHistoryItemViews)
        .extracting(
            CaseHistoryItemView::getDateTime,
            CaseHistoryItemView::getPersonId,
            CaseHistoryItemView::getPersonName
)
        .containsExactly(
            tuple(firstCaseHistory.getDateTime(),  person1.getId(),  person1.getFullName()),
            tuple(secondCaseHistoryWithFiles.getDateTime(),  person2.getId(),  person2.getFullName())
        );
  }

  @Test
  public void getCaseHistory_whenHistoryItemsReturned_andPersonEmailLabelIsSet() {
    firstCaseHistory = new CaseHistoryItemView.Builder(HISTORY_ITEM_HEADER, firstCaseHistoryInstant, person1.getId())
        .setPersonEmailLabel("Email")
        .addDataItem(NOTE_LABEL, NOTE_NO_FILES_TEXT)
        .build();

    when(caseNoteService.getCaseHistoryItemViews(any())).thenReturn(List.of(firstCaseHistory));

    when(applicationSubmissionCaseHistoryItemService.getCaseHistoryItemViews(any())).thenReturn(List.of());


    var caseHistoryItemViews = caseHistoryService.getCaseHistory(pwaApplication);

    assertThat(caseHistoryItemViews)
        .extracting(
            CaseHistoryItemView::getDateTime,
            CaseHistoryItemView::getPersonId,
            CaseHistoryItemView::getPersonName,
            CaseHistoryItemView::getPersonEmail
        )
        .containsExactly(
            tuple(firstCaseHistory.getDateTime(),  person1.getId(),  person1.getFullName(), person1.getEmailAddress())
        );
  }

  @Test
  public void getCaseHistory_whenHistoryItemsReturned_andPersonEmailLabelIsNotSet() {
    firstCaseHistory = new CaseHistoryItemView.Builder(HISTORY_ITEM_HEADER, firstCaseHistoryInstant, person1.getId())
        .addDataItem(NOTE_LABEL, NOTE_NO_FILES_TEXT)
        .build();

    when(caseNoteService.getCaseHistoryItemViews(any())).thenReturn(List.of(firstCaseHistory));

    when(applicationSubmissionCaseHistoryItemService.getCaseHistoryItemViews(any())).thenReturn(List.of());


    var caseHistoryItemViews = caseHistoryService.getCaseHistory(pwaApplication);

    assertThat(caseHistoryItemViews)
        .extracting(
            CaseHistoryItemView::getDateTime,
            CaseHistoryItemView::getPersonId,
            CaseHistoryItemView::getPersonName,
            CaseHistoryItemView::getPersonEmail
        )
        .containsExactly(
            tuple(firstCaseHistory.getDateTime(),  person1.getId(),  person1.getFullName(), null)
        );
  }

}
