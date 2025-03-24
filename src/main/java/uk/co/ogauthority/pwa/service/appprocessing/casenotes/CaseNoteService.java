package uk.co.ogauthority.pwa.service.appprocessing.casenotes;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.controller.appprocessing.casenotes.CaseNoteFileManagementRestController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileUploadRestController;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNoteDocumentLink;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteDocumentLinkRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.casenotes.CaseNoteRepository;
import uk.co.ogauthority.pwa.service.appprocessing.casehistory.CaseHistoryItemService;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.validators.appprocessing.casenote.CaseNoteFormValidator;

@Service
public class CaseNoteService implements AppProcessingService, CaseHistoryItemService {

  private final CaseNoteRepository caseNoteRepository;
  private final AppFileService appFileService;
  private final Clock clock;
  private final CaseNoteDocumentLinkRepository caseNoteDocumentLinkRepository;
  private final CaseNoteFormValidator validator;
  private final AppFileManagementService appFileManagementService;

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CASE_NOTES;
  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CASE_NOTES;
  private final FileManagementService fileManagementService;

  @Autowired
  public CaseNoteService(CaseNoteRepository caseNoteRepository,
                         AppFileService appFileService,
                         @Qualifier("utcClock") Clock clock,
                         CaseNoteDocumentLinkRepository caseNoteDocumentLinkRepository,
                         CaseNoteFormValidator validator,
                         AppFileManagementService appFileManagementService,
                         FileManagementService fileManagementService
  ) {
    this.caseNoteRepository = caseNoteRepository;
    this.appFileService = appFileService;
    this.clock = clock;
    this.caseNoteDocumentLinkRepository = caseNoteDocumentLinkRepository;
    this.validator = validator;
    this.appFileManagementService = appFileManagementService;
    this.fileManagementService = fileManagementService;
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

    appFileManagementService.saveFiles(form, application, DOCUMENT_TYPE);

    var fileIds = form.getUploadedFiles().stream()
        .map(UploadedFileForm::getFileId)
        .map(String::valueOf)
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

    var appFileIdToViewMap = appFileManagementService.getUploadedFileViews(pwaApplication, DOCUMENT_TYPE).stream()
        .collect(Collectors.toMap(UploadedFileView::getFileId, Function.identity()));

    var caseNoteIdToDocLinksMap = caseNoteDocumentLinkRepository.findAllByCaseNoteIn(caseNotes).stream()
        .collect(Collectors.groupingBy(docLink -> docLink.getCaseNote().getId()));

    var caseNoteFileDownloadUrl = ReverseRouter.route(on(CaseNoteFileManagementRestController.class)
        .download(pwaApplication.getId(), null)
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

  public BindingResult validate(AddCaseNoteForm form, BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    return bindingResult;
  }

  public FileUploadComponentAttributes getFileUploadComponentAttributes(
      List<UploadedFileForm> existingFiles,
      PwaApplication pwaApplication
  ) {
    var controller = CaseNoteFileManagementRestController.class;

    return fileManagementService.getFileUploadComponentAttributesBuilder(existingFiles, DOCUMENT_TYPE)
        .withUploadUrl(ReverseRouter.route(on(AppFileUploadRestController.class)
            .upload(pwaApplication.getId(), FILE_PURPOSE.name(), null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(pwaApplication.getId(), null)))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(pwaApplication.getId(), null)))
        .build();
  }

}
