package uk.co.ogauthority.pwa.service.docgen;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;

import java.time.Instant;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.exception.documents.DocgenException;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.ConsentDocumentFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatusResult;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.docgen.DocgenRunRepository;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentCreationService;

@Service
public class DocgenService {

  public static final String FILENAME = "%s consent document.pdf";
  public static final String PREVIEW_FILENAME = "%s consent preview.pdf";
  public static final String PDF_FILE_TYPE = "application/pdf";

  private final Scheduler scheduler;
  private final DocgenRunRepository docgenRunRepository;
  private final DocumentCreationService documentCreationService;
  private final AppFileManagementService appFileManagementService;
  private final FileService fileService;
  private final ConsentDocumentFileManagementService consentDocumentFileManagementService;

  @Autowired
  public DocgenService(Scheduler scheduler,
                       DocgenRunRepository docgenRunRepository,
                       DocumentCreationService documentCreationService,
                       AppFileManagementService appFileManagementService,
                       FileService fileService,
                       ConsentDocumentFileManagementService consentDocumentFileManagementService
  ) {
    this.scheduler = scheduler;
    this.docgenRunRepository = docgenRunRepository;
    this.documentCreationService = documentCreationService;
    this.appFileManagementService = appFileManagementService;
    this.fileService = fileService;
    this.consentDocumentFileManagementService = consentDocumentFileManagementService;
  }

  /**
   * Schedule a new job to trigger immediately to generate a docgen run.
   * @param docgenRun to create the document for
   */
  @Transactional
  public void scheduleDocumentGeneration(DocgenRun docgenRun) {

    try {

      // add to scheduler
      JobKey jobKey = jobKey(String.valueOf(docgenRun.getId()), "DocGen");
      JobDetail jobDetail = newJob(DocgenSchedulerBean.class)
          .withIdentity(jobKey)
          .requestRecovery()
          .storeDurably()
          .build();

      scheduler.addJob(jobDetail, false);
      scheduler.triggerJob(jobKey);

    } catch (Exception e) {
      throw new RuntimeException("Error scheduling docgen run job", e);
    }

  }

  public DocgenRun createDocgenRun(DocumentInstance documentInstance, DocGenType docGenType, Person person) {

    DocgenRun docgenRun = new DocgenRun(documentInstance, docGenType, DocgenRunStatus.PENDING);
    docgenRun.setScheduledOn(Instant.now());
    docgenRun.setScheduledByPerson(person);

    return docgenRunRepository.save(docgenRun);

  }

  /**
   * Get a status object for the given docgen run.
   * @param docgenRunId The docgen run id
   * @return A DocgenRunStatusResult
   */
  public DocgenRunStatusResult getDocgenRunStatus(Long docgenRunId, String completeUrl) {
    var docgenRun = getDocgenRun(docgenRunId);
    return new DocgenRunStatusResult(docgenRun, completeUrl);
  }

  public DocgenRun getDocgenRun(Long docgenRunId) {
    return docgenRunRepository.findById(docgenRunId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Docgen run with id %s not found", docgenRunId)));
  }

  public void processPreviewDocgenRun(DocgenRun docgenRun) {

    docgenRun.setStartedOn(Instant.now());

    try {
      processAndCompletePreviewRun(docgenRun);
    } catch (Exception e) {
      markRunFailed(docgenRun);
      throw new DocgenException("Error generating docgen run Id: " + docgenRun.getId(), e);
    }

  }

  private void processAndCompletePreviewRun(DocgenRun docgenRun) {

    var docResource = documentCreationService.createConsentDocument(docgenRun);

    uploadPreviewDocument(docgenRun, docResource);

    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setCompletedOn(Instant.now());
    docgenRunRepository.save(docgenRun);

  }

  private void uploadPreviewDocument(DocgenRun docgenRun, ByteArrayResource docResource) {
    var pwaApplication = docgenRun.getDocumentInstance().getPwaApplication();

    var filename = PREVIEW_FILENAME.formatted(pwaApplication.getAppReference().replace("/", "-"));

    var uploadedFile = handleDocumentUpload(docResource, filename);

    appFileManagementService.saveConsentPreview(uploadedFile, pwaApplication);
  }

  public void processConsentDocgenRun(DocgenRun docgenRun, PwaConsent pwaConsent) {

    docgenRun.setStartedOn(Instant.now());

    try {
      processAndCompleteConsentRun(docgenRun, pwaConsent);
    } catch (Exception e) {
      markRunFailed(docgenRun);
      throw new DocgenException("Error generating docgen run Id: " + docgenRun.getId(), e);
    }

  }

  private void processAndCompleteConsentRun(DocgenRun docgenRun, PwaConsent pwaConsent) {

    var docResource = documentCreationService.createConsentDocument(docgenRun);

    uploadConsentDocument(docResource, pwaConsent);

    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setCompletedOn(Instant.now());
    docgenRunRepository.save(docgenRun);

  }

  private void uploadConsentDocument(ByteArrayResource docResource, PwaConsent pwaConsent) {
    var filename = FILENAME.formatted(pwaConsent.getReference().replace("/", "-"));

    var uploadedFile = handleDocumentUpload(docResource, filename);

    consentDocumentFileManagementService.saveConsentDocument(uploadedFile, pwaConsent);
  }

  private UploadedFileForm handleDocumentUpload(ByteArrayResource docResource, String filename) {
    var fileSource = FileSource.fromInputStreamSource(
        docResource,
        filename,
        PDF_FILE_TYPE,
        docResource.contentLength()
    );

    var uploadResponse = fileService.upload(builder -> builder
        .withFileSource(fileSource)
        .build()
    );

    if (uploadResponse.getError() != null) {
      throw new DocgenException(uploadResponse.getError());
    }

    var uploadedFile = new UploadedFileForm();
    uploadedFile.setFileId(uploadResponse.getFileId());
    uploadedFile.setFileName(filename);
    uploadedFile.setFileDescription("");
    uploadedFile.setFileSize(uploadedFile.getUploadedFileSize());
    uploadedFile.setFileUploadedAt(Instant.now());

    return uploadedFile;
  }

  private void markRunFailed(DocgenRun docgenRun) {
    docgenRun.setStatus(DocgenRunStatus.FAILED);
    docgenRun.setCompletedOn(Instant.now());
    docgenRunRepository.save(docgenRun);
  }
}
