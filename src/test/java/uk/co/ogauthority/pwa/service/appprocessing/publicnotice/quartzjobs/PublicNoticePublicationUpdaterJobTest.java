package uk.co.ogauthority.pwa.service.appprocessing.publicnotice.quartzjobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.FinalisePublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeTestUtil;

@ExtendWith(MockitoExtension.class)
class PublicNoticePublicationUpdaterJobTest {

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private FinalisePublicNoticeService finalisePublicNoticeService;

  @Mock
  private JobExecutionContext jobExecutionContext; // mocking an interface we dont control is generally a bad idea. Not sure how to avoid here.

  private PublicNoticePublicationUpdaterJob publicNoticePublicationUpdaterJob;

  @BeforeEach
  void setUp() throws Exception {
    publicNoticePublicationUpdaterJob = new PublicNoticePublicationUpdaterJob(
        publicNoticeService, finalisePublicNoticeService);

  }

  @Test
  void executeInternal_whenNoPublicNoticesToPublish() throws JobExecutionException {

    publicNoticePublicationUpdaterJob.executeInternal(jobExecutionContext);

    verifyNoInteractions(finalisePublicNoticeService);
  }

  @Test
  void executeInternal_whenPublicNoticesDueToPublish() throws JobExecutionException {

    var publicNotices = List.of(PublicNoticeTestUtil.createWaitingPublicNotice(null),
        PublicNoticeTestUtil.createWaitingPublicNotice(null));
    when(publicNoticeService.getAllPublicNoticesDueForPublishing()).thenReturn(publicNotices);

    publicNoticePublicationUpdaterJob.executeInternal(jobExecutionContext);

    publicNotices.forEach(publicNotice ->
        verify(finalisePublicNoticeService, times(2)).publishPublicNotice(publicNotice));
  }

  @Test
  void executeInternal_whenNoPublicNoticesToEnd() throws JobExecutionException {

    publicNoticePublicationUpdaterJob.executeInternal(jobExecutionContext);
    verify(publicNoticeService, never()).endPublicNotices(any());
  }

  @Test
  void executeInternal_whenPublicNoticesToEndIsDue() throws JobExecutionException {

    var publicNotices = List.of(PublicNoticeTestUtil.createPublishedPublicNotice(null),
        PublicNoticeTestUtil.createPublishedPublicNotice(null));
    when(publicNoticeService.getAllPublicNoticesDueToEnd()).thenReturn(publicNotices);

    publicNoticePublicationUpdaterJob.executeInternal(jobExecutionContext);

    verify(publicNoticeService, times(1)).endPublicNotices(publicNotices);
  }





}