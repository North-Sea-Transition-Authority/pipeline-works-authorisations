package uk.co.ogauthority.pwa.service.appprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNoteDocumentLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.appprocessing.casehistory.CaseHistoryItemType;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.DataItemRow;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteServiceTest {

  private CaseNoteService caseNoteService;

  @Mock
  private CaseNoteRepository caseNoteRepository;

  @Mock
  private AppFileService appFileService;

  @Mock
  private CaseNoteDocumentLinkRepository documentLinkRepository;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Captor
  private ArgumentCaptor<CaseNote> caseNoteCaptor;

  @Captor
  private ArgumentCaptor<List<CaseNoteDocumentLink>> caseNoteDocumentLinksCaptor;

  @Before
  public void setUp() {
    caseNoteService = new CaseNoteService(caseNoteRepository, appFileService, clock, documentLinkRepository);
  }

  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.ADD_CASE_NOTE), null,
        null);

    boolean canShow = caseNoteService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null,
        null);

    boolean canShow = caseNoteService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void createCaseNote_noDocuments() {

    var app = new PwaApplication();
    var person = new Person(1, null, null, null, null);
    var user = new WebUserAccount(1, person);

    var form = new AddCaseNoteForm();
    form.setNoteText("some note text");

    caseNoteService.createCaseNote(app, form, user);

    verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture());

    verify(appFileService, times(1)).updateFiles(form, app, AppFilePurpose.CASE_NOTES, FileUpdateMode.KEEP_UNLINKED_FILES, user);

    var caseNote = caseNoteCaptor.getValue();

    assertThat(caseNote.getPwaApplication()).isEqualTo(app);
    assertThat(caseNote.getPersonId()).isEqualTo(person.getId());
    assertThat(caseNote.getDateTime()).isEqualTo(clock.instant());
    assertThat(caseNote.getItemType()).isEqualTo(CaseHistoryItemType.CASE_NOTE);
    assertThat(caseNote.getNoteText()).isEqualTo("some note text");

  }

  @Test
  public void createCaseNote_withDocuments() {

    var app = new PwaApplication();
    var person = new Person(1, null, null, null, null);
    var user = new WebUserAccount(1, person);

    var form = new AddCaseNoteForm();
    form.setNoteText("some note text");
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("id", "desc", Instant.now()),
        new UploadFileWithDescriptionForm("id2", "desc2", Instant.now().minusSeconds(1))
    ));

    var appFile1 = new AppFile(app, "id", AppFilePurpose.CASE_NOTES, ApplicationFileLinkStatus.FULL);
    var appFile2 = new AppFile(app, "id2", AppFilePurpose.CASE_NOTES, ApplicationFileLinkStatus.FULL);

    when(appFileService.getFilesByIdIn(eq(app), eq(AppFilePurpose.CASE_NOTES), any())).thenReturn(List.of(
        appFile1,
        appFile2
    ));

    caseNoteService.createCaseNote(app, form, user);

    verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture());

    verify(appFileService, times(1)).updateFiles(form, app, AppFilePurpose.CASE_NOTES, FileUpdateMode.KEEP_UNLINKED_FILES, user);

    var caseNote = caseNoteCaptor.getValue();

    assertThat(caseNote.getPwaApplication()).isEqualTo(app);
    assertThat(caseNote.getPersonId()).isEqualTo(person.getId());
    assertThat(caseNote.getDateTime()).isEqualTo(clock.instant());
    assertThat(caseNote.getItemType()).isEqualTo(CaseHistoryItemType.CASE_NOTE);
    assertThat(caseNote.getNoteText()).isEqualTo("some note text");

    verify(documentLinkRepository, times(1)).saveAll(caseNoteDocumentLinksCaptor.capture());

    assertThat(caseNoteDocumentLinksCaptor.getValue())
        .extracting(CaseNoteDocumentLink::getCaseNote, CaseNoteDocumentLink::getAppFile)
        .containsExactlyInAnyOrder(
            tuple(caseNote, appFile1),
            tuple(caseNote, appFile2)
        );

  }

  @Test
  public void getCaseHistoryItemViews() {

    var app = new PwaApplication();

    var caseNote1 = new CaseNote(app, new PersonId(1), clock.instant(), "noteText");
    caseNote1.setId(10);
    var caseNote2 = new CaseNote(app, new PersonId(2), clock.instant().minusSeconds(10), "note2");
    caseNote2.setId(11);

    when(caseNoteRepository.getAllByPwaApplication(any())).thenReturn(List.of(
        caseNote1,
        caseNote2
    ));

    var fileView1 = new UploadedFileView("id", "name", 1L, "desc", clock.instant(), "#id");
    var fileView2 = new UploadedFileView("id2", "abc", 2L, "desc2", clock.instant().minusSeconds(10), "#id2");
    when(appFileService.getUploadedFileViews(app, AppFilePurpose.CASE_NOTES, ApplicationFileLinkStatus.FULL)).thenReturn(
        List.of(fileView1, fileView2));

    var appFile1 = new AppFile();
    appFile1.setFileId("id");
    var docLink1 = new CaseNoteDocumentLink(caseNote1, appFile1);

    var appFile2 = new AppFile();
    appFile2.setFileId("id2");
    var docLink2 = new CaseNoteDocumentLink(caseNote1, appFile2);

    when(documentLinkRepository.findAllByCaseNoteIn(any())).thenReturn(List.of(docLink1, docLink2));

    var views = caseNoteService.getCaseHistoryItemViews(app);

    assertThat(views)
        .extracting(
            CaseHistoryItemView::getDateTime,
            CaseHistoryItemView::getDateTimeDisplay,
            CaseHistoryItemView::getHeaderText,
            CaseHistoryItemView::getPersonId,
            CaseHistoryItemView::getPersonLabelText,
            CaseHistoryItemView::getPersonName,
            CaseHistoryItemView::getDataItemRows)
        .contains(
            tuple(clock.instant(), DateUtils.formatDateTime(clock.instant()), "Case note", new PersonId(1), "Created by", null, List.of(new DataItemRow(Map.of("Note text", "noteText")))),
            tuple(clock.instant().minusSeconds(10), DateUtils.formatDateTime(clock.instant().minusSeconds(10)), "Case note", new PersonId(2), "Created by", null, List.of(new DataItemRow(Map.of("Note text", "note2"))))
        );

    views.forEach(view -> {

      if (view.getDataItemRows().get(0).getDataItems().containsValue("note2")) {

        assertThat(view.getUploadedFileViews()).isEmpty();

      } else {

        // files sorted by name
        assertThat(view.getUploadedFileViews())
            .extracting(UploadedFileView::getFileName)
            .containsExactly("abc", "name");

      }

    });

  }

}
