package uk.co.ogauthority.pwa.service.appprocessing.publicnotice.quartzjobs;

import java.util.List;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.FinalisePublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;

@Component
public class PublicNoticePublicationUpdaterJob extends QuartzJobBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(PublicNoticePublicationUpdaterJob.class);
  private static final String LOGGER_JOB_STRING = "Public notice publication update job";

  private final PublicNoticeService publicNoticeService;
  private final FinalisePublicNoticeService finalisePublicNoticeService;

  @Autowired
  public PublicNoticePublicationUpdaterJob(
      PublicNoticeService publicNoticeService,
      FinalisePublicNoticeService finalisePublicNoticeService) {
    this.publicNoticeService = publicNoticeService;
    this.finalisePublicNoticeService = finalisePublicNoticeService;

  }


  private void publishPublicNotices(List<PublicNotice> publicNoticesToPublish) {
    for (PublicNotice publicNotice : publicNoticesToPublish) {
      LOGGER.debug("publishing attempt id:{}", publicNotice.getId());
      finalisePublicNoticeService.publishPublicNotice(publicNotice);
      LOGGER.debug("Published public notice id:{}", publicNotice.getId());
    }
  }

  private void endPublicNotices(List<PublicNotice> publicNoticesToEnd) {
    LOGGER.debug("ending public notices attempt");
    publicNoticeService.endPublicNotices(publicNoticesToEnd);
    LOGGER.debug("ended public notices");
  }

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

    try {
      LOGGER.info("Executing {} ... [isRecovering = {}]", LOGGER_JOB_STRING, context.isRecovering());

      var publicNoticesToPublish = publicNoticeService.getAllPublicNoticesDueForPublishing();
      var publicNoticesToEnd = publicNoticeService.getAllPublicNoticesDueToEnd();

      if (!publicNoticesToPublish.isEmpty()) {
        LOGGER.info("Found {} public notice/s to publish", publicNoticesToPublish.size());
        publishPublicNotices(publicNoticesToPublish);
      }

      if (!publicNoticesToEnd.isEmpty()) {
        LOGGER.info("Found {} public notice/s to end", publicNoticesToEnd.size());
        endPublicNotices(publicNoticesToEnd);
      }

      LOGGER.info("Job execution complete for {}", LOGGER_JOB_STRING);

    } catch (Exception e) {
      throw new JobExecutionException("Publication update job execution failed", e);
    }

  }
}
