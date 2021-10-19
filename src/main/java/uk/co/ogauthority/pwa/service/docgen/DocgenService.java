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
import uk.co.ogauthority.pwa.exception.documents.DocgenException;
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

  public void processDocgenRun(DocgenRun docgenRun) {

    docgenRun.setStartedOn(Instant.now());

    try {
      processAndCompleteRun(docgenRun);
    } catch (Exception e) {
      markRunFailed(docgenRun);
      throw new DocgenException("Error generating docgen run Id: " + docgenRun.getId(), e);
    }

  }

  private void processAndCompleteRun(DocgenRun docgenRun) {

    var docBlob = documentCreationService.createConsentDocument(docgenRun);

    docgenRun.setGeneratedDocument(docBlob);
    docgenRun.setStatus(DocgenRunStatus.COMPLETE);
    docgenRun.setCompletedOn(Instant.now());
    docgenRunRepository.save(docgenRun);

  }

  private void markRunFailed(DocgenRun docgenRun) {
    docgenRun.setStatus(DocgenRunStatus.FAILED);
    docgenRun.setCompletedOn(Instant.now());
    docgenRunRepository.save(docgenRun);
  }

}
