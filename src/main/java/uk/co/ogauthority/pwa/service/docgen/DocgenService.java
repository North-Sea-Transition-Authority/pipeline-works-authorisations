package uk.co.ogauthority.pwa.service.docgen;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;

import java.time.Instant;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatusResult;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.repository.docgen.DocgenRunRepository;
import uk.co.ogauthority.pwa.service.documents.generation.DocumentCreationService;

@Service
public class DocgenService {

  private final Scheduler scheduler;
  private final DocgenRunRepository docgenRunRepository;
  private final DocumentCreationService documentCreationService;

  @Autowired
  public DocgenService(Scheduler scheduler,
                       DocgenRunRepository docgenRunRepository,
                       DocumentCreationService documentCreationService) {
    this.scheduler = scheduler;
    this.docgenRunRepository = docgenRunRepository;
    this.documentCreationService = documentCreationService;
  }

  /**
   * Create a new DocgenRun and schedule a new job to trigger immediately.
   * @param documentInstance to create the document for
   * @return A Pending DocgenRun
   */
  @Transactional
  public DocgenRun scheduleDocumentGeneration(DocumentInstance documentInstance,
                                              DocGenType docGenType,
                                              Person person) {

    try {
      // create pending docgen run
      DocgenRun docgenRun = new DocgenRun(documentInstance, docGenType, DocgenRunStatus.PENDING);
      docgenRun.setScheduledOn(Instant.now());
      docgenRun.setScheduledByPerson(person);

      docgenRunRepository.save(docgenRun);

      // add to scheduler
      JobKey jobKey = jobKey(String.valueOf(docgenRun.getId()), "DocGen");
      JobDetail jobDetail = newJob(DocgenSchedulerBean.class)
          .withIdentity(jobKey)
          .usingJobData("docgenType", docGenType.name())
          .requestRecovery()
          .storeDurably()
          .build();

      scheduler.addJob(jobDetail, false);
      scheduler.triggerJob(jobKey);

      return docgenRun;

    } catch (Exception e) {
      throw new RuntimeException("Error scheduling docgen run job", e);
    }

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

  void processAndCompleteRun(DocgenRun docgenRun,
                             DocGenType docGenType) {

    var docBlob = documentCreationService
        .createConsentDocument(docgenRun.getDocumentInstance(), docGenType);

    docgenRun.setGeneratedDocument(docBlob);
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setCompletedOn(Instant.now());
    docgenRunRepository.save(docgenRun);

  }

  void markRunFailed(DocgenRun docgenRun) {
    docgenRun.setStatus(DocgenRunStatus.FAILED);
    docgenRun.setCompletedOn(Instant.now());
    docgenRunRepository.save(docgenRun);
  }

}
