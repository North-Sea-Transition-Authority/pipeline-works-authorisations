package uk.co.ogauthority.pwa.service.appprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.controller.appprocessing.casenotes.CaseNoteFileManagementRestController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileUploadRestController;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNoteDocumentLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.appprocessing.casehistory.CaseHistoryItemType;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.DataItemRow;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteRepository;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.appprocessing.casenote.CaseNoteFormValidator;

@ExtendWith(MockitoExtension.class)
class CaseNoteServiceTest {

  private CaseNoteService caseNoteService;

  @Mock
  private CaseNoteRepository caseNoteRepository;

  @Mock
  private AppFileService appFileService;

  @Mock
  private CaseNoteDocumentLinkRepository documentLinkRepository;

  @Mock
  private CaseNoteFormValidator validator;

  @Mock
  private AppFileManagementService appFileManagementService;

  @Mock
  private FileManagementService fileManagementService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CASE_NOTES;
  
  @Captor
  private ArgumentCaptor<CaseNote> caseNoteCaptor;

  @Captor
  private ArgumentCaptor<List<CaseNoteDocumentLink>> caseNoteDocumentLinksCaptor;

  @BeforeEach
  void setUp() {
    caseNoteService = new CaseNoteService(
        caseNoteRepository,
        appFileService,
        clock,
        documentLinkRepository,
        validator,
        appFileManagementService,
        fileManagementService
    );
  }

  @Test
  void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.ADD_CASE_NOTE), null,
        null, Set.of());

    boolean canShow = caseNoteService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null,
        null, Set.of());

    boolean canShow = caseNoteService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void createCaseNote_noDocuments() {

    var app = new PwaApplication();
    var person = new Person(1, null, null, null, null);
    var user = new WebUserAccount(1, person);

    var form = new AddCaseNoteForm();
    form.setNoteText("some note text");

    caseNoteService.createCaseNote(app, form, user);

    verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture());

    var caseNote = caseNoteCaptor.getValue();

    assertThat(caseNote.getPwaApplication()).isEqualTo(app);
    assertThat(caseNote.getPersonId()).isEqualTo(person.getId());
    assertThat(caseNote.getDateTime()).isEqualTo(clock.instant());
    assertThat(caseNote.getItemType()).isEqualTo(CaseHistoryItemType.CASE_NOTE);
    assertThat(caseNote.getNoteText()).isEqualTo("some note text");

  }

  @Test
  void createCaseNote_withDocuments() {

    var app = new PwaApplication();
    var person = new Person(1, null, null, null, null);
    var user = new WebUserAccount(1, person);

    var form = new AddCaseNoteForm();
    form.setNoteText("some note text");
    form.setUploadedFiles(List.of(
        FileManagementValidatorTestUtils.createUploadedFileForm(),
        FileManagementValidatorTestUtils.createUploadedFileForm()
    ));

    var appFile1 = new AppFile(app, "id", AppFilePurpose.CASE_NOTES, ApplicationFileLinkStatus.FULL);
    var appFile2 = new AppFile(app, "id2", AppFilePurpose.CASE_NOTES, ApplicationFileLinkStatus.FULL);

    when(appFileService.getFilesByIdIn(eq(app), eq(AppFilePurpose.CASE_NOTES), any())).thenReturn(List.of(
        appFile1,
        appFile2
    ));

    caseNoteService.createCaseNote(app, form, user);

    verify(caseNoteRepository, times(1)).save(caseNoteCaptor.capture());

    verify(appFileManagementService, times(1)).saveFiles(form, app, DOCUMENT_TYPE);

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
  void getCaseHistoryItemViews() {

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
    when(appFileManagementService.getUploadedFileViews(app, DOCUMENT_TYPE)).thenReturn(List.of(fileView1, fileView2));

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

  @Test
  void fileUploadComponentAttributes_VerifyMethodCall() {
    var app = new PwaApplication();

    List<UploadedFileForm> existingFileForms = Collections.emptyList();

    var builder = FileUploadComponentAttributes.newBuilder()
        .withMaximumSize(DataSize.ofBytes(1));
    when(fileManagementService.getFileUploadComponentAttributesBuilder(existingFileForms, DOCUMENT_TYPE))
        .thenReturn(builder);

    assertThat(caseNoteService.getFileUploadComponentAttributes(existingFileForms, app))
        .extracting(
            FileUploadComponentAttributes::uploadUrl,
            FileUploadComponentAttributes::downloadUrl,
            FileUploadComponentAttributes::deleteUrl
        ).containsExactly(
            ReverseRouter.route(on(AppFileUploadRestController.class).upload(app.getId(), DOCUMENT_TYPE.name(), null)),
            ReverseRouter.route(on(CaseNoteFileManagementRestController.class).download(app.getId(), null)),
            ReverseRouter.route(on(CaseNoteFileManagementRestController.class).delete(app.getId(), null))
        );

    verify(fileManagementService).getFileUploadComponentAttributesBuilder(existingFileForms, DOCUMENT_TYPE);
  }
}
