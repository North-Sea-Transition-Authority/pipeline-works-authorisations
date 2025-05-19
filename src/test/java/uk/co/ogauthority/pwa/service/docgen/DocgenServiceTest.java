package uk.co.ogauthority.pwa.service.docgen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quartz.JobKey.jobKey;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.core.io.ByteArrayResource;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.ConsentDocumentFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.docgen.DocgenRunRepository;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentCreationService;

@ExtendWith(MockitoExtension.class)
class DocgenServiceTest {

  @Mock
  private Scheduler scheduler;

  @Mock
  private DocgenRunRepository docgenRunRepository;

  @Mock
  private DocumentCreationService documentCreationService;
  
  @Mock
  private FileService fileService;
  
  @Mock
  private AppFileManagementService appFileManagementService;

  @Mock
  private ConsentDocumentFileManagementService consentDocumentFileManagementService;

  private DocgenService docgenService;

  @Captor
  private ArgumentCaptor<DocgenRun> docgenRunCaptor;

  @Captor
  private ArgumentCaptor<JobDetail> jobDetailCaptor;

  @Captor
  private ArgumentCaptor<UploadedFileForm> uploadedFileCaptor;

  private final Person person = PersonTestUtil.createDefaultPerson();
  private DocumentInstance documentInstance;
  private PwaApplication application;
  
  @BeforeEach
  void setUp() {

    docgenService = new DocgenService(scheduler, docgenRunRepository, documentCreationService, appFileManagementService, fileService, consentDocumentFileManagementService);

    application = new PwaApplication();
    application.setAppReference("app reference");
    application.setConsentReference("consent reference");
    
    documentInstance = new DocumentInstance();
    documentInstance.setPwaApplication(application);

  }

  @Test
  void scheduleDocumentGeneration() throws SchedulerException {

    var docInstance = new DocumentInstance();
    var run = new DocgenRun();
    run.setDocumentInstance(docInstance);
    run.setDocGenType(DocGenType.FULL);
    run.setScheduledByPerson(person);

    docgenService.scheduleDocumentGeneration(run);

    verify(scheduler).addJob(jobDetailCaptor.capture(), eq(false));

    assertThat(jobDetailCaptor.getValue()).satisfies(jobDetail -> {
      assertThat(jobDetail.getKey()).isEqualTo(jobKey(String.valueOf(run.getId()), "DocGen"));
      assertThat(jobDetail.isDurable()).isTrue();
      assertThat(jobDetail.requestsRecovery()).isTrue();
    });

    var jobDetail = jobDetailCaptor.getValue();

    verify(scheduler).triggerJob(jobDetail.getKey());

  }

  @Test
  void createDocgenRun() {

    docgenService.createDocgenRun(documentInstance, DocGenType.FULL, person);

    verify(docgenRunRepository).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(run -> {
      assertThat(run.getDocumentInstance()).isEqualTo(documentInstance);
      assertThat(run.getDocGenType()).isEqualTo(DocGenType.FULL);
      assertThat(run.getStatus()).isEqualTo(DocgenRunStatus.PENDING);
      assertThat(run.getStartedOn()).isNull();
      assertThat(run.getScheduledOn()).isNotNull();
      assertThat(run.getScheduledByPerson()).isEqualTo(person);
      assertThat(run.getCompletedOn()).isNull();
      assertThat(run.getGeneratedDocument()).isNull();
    });

  }

  @Test
  void getDocgenRunStatus() {

    var docgenRun = new DocgenRun();
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setId(1L);

    when(docgenRunRepository.findById(1L)).thenReturn(Optional.of(docgenRun));

    var status = docgenService.getDocgenRunStatus(docgenRun.getId(), "url");

    assertThat(status.getDocgenRunId()).isEqualTo(docgenRun.getId());
    assertThat(status.getStatus()).isEqualTo(docgenRun.getStatus());
    assertThat(status.getOnCompleteUrl()).isEqualTo("url");

  }

  @Test
  void getDocgenRun_found() {

    var docgenRun = new DocgenRun();
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setId(1L);

    when(docgenRunRepository.findById(1L)).thenReturn(Optional.of(docgenRun));

    assertThat(docgenService.getDocgenRun(1L)).isEqualTo(docgenRun);

  }

  @Test
  void getDocgenRun_notFound() {
    when(docgenRunRepository.findById(anyLong())).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->

      docgenService.getDocgenRun(1L));

  }

  @Test
  void processPreviewDocgenRun_complete() {

    var run = new DocgenRun();
    run.setDocGenType(DocGenType.PREVIEW);
    run.setDocumentInstance(documentInstance);
    var byteArrayResource = new ByteArrayResource(new byte[1]);

    when(documentCreationService.createConsentDocument(run))
        .thenReturn(byteArrayResource);

    var response = FileUploadResponse.success(UUID.randomUUID(), mock(FileSource.class));

    when(fileService.upload(any())).thenReturn(response);

    docgenService.processPreviewDocgenRun(run);

    verify(documentCreationService)
        .createConsentDocument(run);

    verify(docgenRunRepository).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(docgenRun -> {
      assertThat(docgenRun.getGeneratedDocument()).isNull();
      assertThat(docgenRun.getStatus()).isEqualTo(DocgenRunStatus.COMPLETE);
      assertThat(docgenRun.getCompletedOn()).isNotNull();
    });

    verify(appFileManagementService).saveConsentPreview(
        uploadedFileCaptor.capture(),
        eq(run.getDocumentInstance().getPwaApplication())
    );

    assertThat(uploadedFileCaptor.getValue()).satisfies(uploadedFileForm -> {
      assertThat(uploadedFileForm.getFileId()).isEqualTo(response.getFileId());
      assertThat(uploadedFileForm.getFileName()).isEqualTo(DocgenService.PREVIEW_FILENAME.formatted(application.getAppReference()));
    });
  }

  @Test
  void processPreviewDocgenRun_failed() {

    var run = new DocgenRun();
    var docInstance = new DocumentInstance();
    run.setDocumentInstance(docInstance);

    when(documentCreationService.createConsentDocument(any())).thenThrow(RuntimeException.class);

    boolean exceptionCaught = false;
    try {
      docgenService.processPreviewDocgenRun(run);
    } catch (Exception e) {
      exceptionCaught = true;
    }

    assertThat(exceptionCaught).isTrue();

    verify(docgenRunRepository).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(docgenRun -> {
      assertThat(docgenRun.getGeneratedDocument()).isNull();
      assertThat(docgenRun.getStatus()).isEqualTo(DocgenRunStatus.FAILED);
      assertThat(docgenRun.getCompletedOn()).isNotNull();
    });

  }

  @Test
  void processConsentDocgenRun_complete() {

    var run = new DocgenRun();
    run.setDocGenType(DocGenType.FULL);
    run.setDocumentInstance(documentInstance);

    var consent = new PwaConsent();
    consent.setReference("reference");

    var byteArrayResource = new ByteArrayResource(new byte[1]);

    when(documentCreationService.createConsentDocument(run))
        .thenReturn(byteArrayResource);

    var response = FileUploadResponse.success(UUID.randomUUID(), mock(FileSource.class));

    when(fileService.upload(any())).thenReturn(response);

    docgenService.processConsentDocgenRun(run, consent);

    verify(documentCreationService)
        .createConsentDocument(run);

    verify(docgenRunRepository).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(docgenRun -> {
      assertThat(docgenRun.getGeneratedDocument()).isNull();
      assertThat(docgenRun.getStatus()).isEqualTo(DocgenRunStatus.COMPLETE);
      assertThat(docgenRun.getCompletedOn()).isNotNull();
    });

    verify(consentDocumentFileManagementService).saveConsentDocument(
        uploadedFileCaptor.capture(),
        eq(consent)
    );

    assertThat(uploadedFileCaptor.getValue()).satisfies(uploadedFileForm -> {
      assertThat(uploadedFileForm.getFileId()).isEqualTo(response.getFileId());
      assertThat(uploadedFileForm.getFileName()).isEqualTo(DocgenService.FILENAME.formatted(consent.getReference()));
    });
  }

  @Test
  void processConsentDocgenRun_failed() {

    var run = new DocgenRun();
    var docInstance = new DocumentInstance();
    run.setDocumentInstance(docInstance);

    when(documentCreationService.createConsentDocument(any())).thenThrow(RuntimeException.class);

    boolean exceptionCaught = false;
    try {
      docgenService.processConsentDocgenRun(run, new PwaConsent());
    } catch (Exception e) {
      exceptionCaught = true;
    }

    assertThat(exceptionCaught).isTrue();

    verify(docgenRunRepository).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(docgenRun -> {
      assertThat(docgenRun.getGeneratedDocument()).isNull();
      assertThat(docgenRun.getStatus()).isEqualTo(DocgenRunStatus.FAILED);
      assertThat(docgenRun.getCompletedOn()).isNotNull();
    });

  }

}