package uk.co.ogauthority.pwa.service.appprocessing.casenotes;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.appprocessing.casenotes.CaseNoteController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNoteDocumentLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteRepository;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryItemService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;

@Service
public class CaseNoteService implements AppProcessingService, CaseHistoryItemService {

  private final CaseNoteRepository caseNoteRepository;
  private final AppFileService appFileService;
  private final Clock clock;
  private final CaseNoteDocumentLinkRepository caseNoteDocumentLinkRepository;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CASE_NOTES;

  @Autowired
  public CaseNoteService(CaseNoteRepository caseNoteRepository,
                         AppFileService appFileService,
                         @Qualifier("utcClock") Clock clock,
                         CaseNoteDocumentLinkRepository caseNoteDocumentLinkRepository) {
    this.caseNoteRepository = caseNoteRepository;
    this.appFileService = appFileService;
    this.clock = clock;
    this.caseNoteDocumentLinkRepository = caseNoteDocumentLinkRepository;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ADD_CASE_NOTE);
  }

  @Transactional
  public void createCaseNote(PwaApplication application,
                             AddCaseNoteForm form,
                             WebUserAccount creatingUser) {

    var caseNote = new CaseNote(
        application,
        creatingUser.getLinkedPerson().getId(),
        Instant.now(clock),
        form.getNoteText());

    caseNoteRepository.save(caseNote);

    // need to keep unlinked files as we are searching across all case notes, don't want to delete files that
    // aren't linked to our specific case note
    appFileService.updateFiles(form, application, FILE_PURPOSE, FileUpdateMode.KEEP_UNLINKED_FILES, creatingUser);

    var fileIds = form.getUploadedFileWithDescriptionForms().stream()
        .map(UploadFileWithDescriptionForm::getUploadedFileId)
        .collect(Collectors.toList());

    if (!fileIds.isEmpty()) {

      var files = appFileService.getFilesByIdIn(application, FILE_PURPOSE, fileIds);

      var documentLinks = new ArrayList<CaseNoteDocumentLink>();
      files.forEach(file -> {

        var caseNoteDocumentLink = new CaseNoteDocumentLink(caseNote, file);
        documentLinks.add(caseNoteDocumentLink);

      });

      caseNoteDocumentLinkRepository.saveAll(documentLinks);

    }

  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {

    var caseNotes = caseNoteRepository.getAllByPwaApplication(pwaApplication);

    var appFileIdToViewMap = appFileService.getUploadedFileViews(pwaApplication, FILE_PURPOSE, ApplicationFileLinkStatus.FULL).stream()
        .collect(Collectors.toMap(UploadedFileView::getFileId, Function.identity()));

    var caseNoteIdToDocLinksMap = caseNoteDocumentLinkRepository.findAllByCaseNoteIn(caseNotes).stream()
        .collect(Collectors.groupingBy(docLink -> docLink.getCaseNote().getId()));

    var caseNoteFileDownloadUrl = ReverseRouter.route(on(CaseNoteController.class)
        .handleDownload(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null)
    );

    return caseNotes.stream()
        .map(caseNote -> {

          var caseNoteFileViews = caseNoteIdToDocLinksMap.getOrDefault(caseNote.getId(), List.of()).stream()
              .map(link -> appFileIdToViewMap.get(link.getAppFile().getFileId()))
              .sorted(Comparator.comparing(UploadedFileView::getFileName))
              .collect(Collectors.toList());

          return new CaseHistoryItemView.Builder("Case note", caseNote.getDateTime(), caseNote.getPersonId())
              .setPersonEmailLabel("Contact email")
              .setUploadedFileViews(caseNoteFileViews, caseNoteFileDownloadUrl)
              .addDataItem("Note text", caseNote.getNoteText())
              .build();

        })
        .collect(Collectors.toList());
  }

  public Optional<CaseNoteDocumentLink> getCaseNoteDocumentLink(PwaApplication application, AppFile appFile) {
    return caseNoteDocumentLinkRepository.findByCaseNote_PwaApplicationAndAppFile(application, appFile);
  }

  @Transactional
  public void deleteFileLink(CaseNoteDocumentLink link) {
    caseNoteDocumentLinkRepository.delete(link);
  }

}
