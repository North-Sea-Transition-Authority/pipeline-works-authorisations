package uk.co.ogauthority.pwa.service.pwaapplications.shared.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.submission.PadPipelineSubmissionRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineNumberingServiceTest {

  @Mock
  private ApplicationContext applicationContext;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadPipelineSubmissionRepository padPipelineSubmissionRepository;

  private PadPipelineNumberingService padPipelineNumberingService;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() {

    padPipelineNumberingService = new PadPipelineNumberingService(padPipelineSubmissionRepository, applicationContext);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    doReturn(padPipelineService)
        .when(applicationContext).getBean(ApplicationTask.PIPELINES.getServiceClass());

  }

  @Test
  public void assignPipelineReferences_canShow_noPipelines() {
    when(padPipelineService.canShowInTaskList(detail)).thenReturn(true);
    when(padPipelineSubmissionRepository.getNonConsentedPipelines(detail)).thenReturn(List.of());

    padPipelineNumberingService.assignPipelineReferences(detail);
    verify(padPipelineService, times(1)).canShowInTaskList(detail);
    verify(padPipelineSubmissionRepository, never()).saveAll(any());
  }

  @Test
  public void assignPipelineReferences_canShow_withPipeline() {
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.CABLE);
    when(padPipelineService.canShowInTaskList(detail)).thenReturn(true);
    when(padPipelineSubmissionRepository.getNonConsentedPipelines(detail)).thenReturn(List.of(padPipeline));

    padPipelineNumberingService.assignPipelineReferences(detail);
    verify(padPipelineService, times(1)).canShowInTaskList(detail);
    verify(padPipelineSubmissionRepository, times(1)).saveAll(List.of(padPipeline));
  }

  @Test
  public void assignPipelineReferences_cannotShow() {
    when(padPipelineService.canShowInTaskList(detail)).thenReturn(false);
    padPipelineNumberingService.assignPipelineReferences(detail);
    verify(padPipelineService, times(1)).canShowInTaskList(detail);
    verify(padPipelineSubmissionRepository, never()).saveAll(any());
  }

  @Test
  public void attachNewReference_singleCore() {
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.CABLE);
    when(padPipelineSubmissionRepository.getNextPipelineReferenceNumber()).thenReturn(1L);

    padPipelineNumberingService.attachNewReference(padPipeline);
    assertThat(padPipeline.getPipelineRef()).isEqualTo(PipelineCoreType.SINGLE_CORE.getReferencePrefix() + "1");
  }

  @Test
  public void attachNewReference_multiCore() {
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.UMBILICAL_JUMPER);
    when(padPipelineSubmissionRepository.getNextPipelineReferenceNumber()).thenReturn(1L);

    padPipelineNumberingService.attachNewReference(padPipeline);
    assertThat(padPipeline.getPipelineRef()).isEqualTo(PipelineCoreType.MULTI_CORE.getReferencePrefix() + "1");
  }
}