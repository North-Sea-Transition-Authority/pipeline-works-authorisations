package uk.co.ogauthority.pwa.service.docgen;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.repository.docgen.DocgenRunRepository;

/**
 * Handles any job executions registered against this bean.
 */
@Component
public class DocgenSchedulerBean extends QuartzJobBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocgenSchedulerBean.class);
  private final DocgenRunRepository docgenRunRepository;
  private final DocgenService docgenService;

  @Autowired
  public DocgenSchedulerBean(DocgenRunRepository docgenRunRepository,
                             DocgenService docgenService) {
    this.docgenRunRepository = docgenRunRepository;
    this.docgenService = docgenService;
  }

  @Override
  protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
    try {

      String docgenRunId = context.getJobDetail().getKey().getName();

      var docgenRun = docgenRunRepository.findById(Long.valueOf(docgenRunId))
          .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Docgen run with id %s not found", docgenRunId)));

      LOGGER.info("Executing job for docgen run {}... [isRecovering = {}]", docgenRun.getId(), context.isRecovering());

      docgenService.processPreviewDocgenRun(docgenRun);

      LOGGER.info("Job execution complete for docgen run {}", docgenRun.getId());

    } catch (Exception e) {
      LOGGER.error("Docgen job execution failed", e);
      throw new JobExecutionException(e);
    }
  }

}