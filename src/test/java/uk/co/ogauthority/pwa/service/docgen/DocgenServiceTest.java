package uk.co.ogauthority.pwa.service.docgen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quartz.JobKey.jobKey;

import java.sql.SQLException;
import java.util.Optional;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.repository.docgen.DocgenRunRepository;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentCreationService;

@RunWith(MockitoJUnitRunner.class)
public class DocgenServiceTest {

  @Mock
  private Scheduler scheduler;

  @Mock
  private DocgenRunRepository docgenRunRepository;

  @Mock
  private DocumentCreationService documentCreationService;

  private DocgenService docgenService;

  @Captor
  private ArgumentCaptor<DocgenRun> docgenRunCaptor;

  @Captor
  private ArgumentCaptor<JobDetail> jobDetailCaptor;

  private final Person person = PersonTestUtil.createDefaultPerson();

  @Before
  public void setUp() throws Exception {

    docgenService = new DocgenService(scheduler, docgenRunRepository, documentCreationService);

  }

  @Test
  public void scheduleDocumentGeneration() throws SchedulerException {

    var docInstance = new DocumentInstance();

    docgenService.scheduleDocumentGeneration(docInstance, DocGenType.FULL, person);

    verify(docgenRunRepository, times(1)).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(docgenRun -> {
      assertThat(docgenRun.getDocumentInstance()).isEqualTo(docInstance);
      assertThat(docgenRun.getDocGenType()).isEqualTo(DocGenType.FULL);
      assertThat(docgenRun.getStatus()).isEqualTo(DocgenRunStatus.PENDING);
      assertThat(docgenRun.getScheduledOn()).isNotNull();
      assertThat(docgenRun.getScheduledByPerson()).isEqualTo(person);
      assertThat(docgenRun.getCompletedOn()).isNull();
      assertThat(docgenRun.getStartedOn()).isNull();
      assertThat(docgenRun.getGeneratedDocument()).isNull();
    });

    verify(scheduler, times(1)).addJob(jobDetailCaptor.capture(), eq(false));

    assertThat(jobDetailCaptor.getValue()).satisfies(jobDetail -> {
      assertThat(jobDetail.getKey()).isEqualTo(jobKey(String.valueOf(docgenRunCaptor.getValue().getId()), "DocGen"));
      assertThat(jobDetail.isDurable()).isTrue();
      assertThat(jobDetail.requestsRecovery()).isTrue();
      assertThat(jobDetail.getJobDataMap()).containsEntry("docgenType", DocGenType.FULL.name());
    });

    var jobDetail = jobDetailCaptor.getValue();

    verify(scheduler, times(1)).triggerJob(jobDetail.getKey());

  }

  @Test
  public void getDocgenRunStatus() {

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
  public void getDocgenRun_found() {

    var docgenRun = new DocgenRun();
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setId(1L);

    when(docgenRunRepository.findById(1L)).thenReturn(Optional.of(docgenRun));

    assertThat(docgenService.getDocgenRun(1L)).isEqualTo(docgenRun);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getDocgenRun_notFound() {

    when(docgenRunRepository.findById(anyLong())).thenReturn(Optional.empty());

    docgenService.getDocgenRun(1L);

  }

  @Test
  public void processAndCompleteRun() throws SQLException {

    var run = new DocgenRun();
    var docInstance = new DocumentInstance();
    run.setDocumentInstance(docInstance);

    var blob = new SerialBlob(new byte[1]);

    when(documentCreationService.createConsentDocument(docInstance, DocGenType.FULL))
        .thenReturn(blob);

    docgenService.processAndCompleteRun(run, DocGenType.FULL);

    verify(documentCreationService, times(1))
        .createConsentDocument(docInstance, DocGenType.FULL);

    verify(docgenRunRepository, times(1)).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(docgenRun -> {
      assertThat(docgenRun.getGeneratedDocument()).isEqualTo(blob);
      assertThat(docgenRun.getStatus()).isEqualTo(DocgenRunStatus.COMPLETE);
      assertThat(docgenRun.getCompletedOn()).isNotNull();
    });

  }

  @Test
  public void markRunFailed() {

    var run = new DocgenRun();

    docgenService.markRunFailed(run);

    verify(docgenRunRepository, times(1)).save(docgenRunCaptor.capture());

    assertThat(docgenRunCaptor.getValue()).satisfies(docgenRun -> {
      assertThat(docgenRun.getGeneratedDocument()).isNull();
      assertThat(docgenRun.getStatus()).isEqualTo(DocgenRunStatus.FAILED);
      assertThat(docgenRun.getCompletedOn()).isNotNull();
    });

  }

}