package uk.co.ogauthority.pwa.features.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTaskService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadPipelineNumberingServiceTest {

  private static final ApplicationTask APP_TASK = ApplicationTask.PIPELINES;

  @Mock
  private ApplicationTaskService applicationTaskService;

  @Mock
  private PadPipelineSubmissionRepository padPipelineSubmissionRepository;


  private PadPipelineNumberingService padPipelineNumberingService;
  private PwaApplicationDetail detail;

  @BeforeEach
  void setUp() {

    padPipelineNumberingService = new PadPipelineNumberingService(padPipelineSubmissionRepository, applicationTaskService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  }

  @Test
  void assignPipelineReferences_canShow_noPipelines() {
    when(applicationTaskService.canShowTask(APP_TASK, detail)).thenReturn(true);
    when(padPipelineSubmissionRepository.getNonConsentedPipelines(detail)).thenReturn(List.of());

    padPipelineNumberingService.assignPipelineReferences(detail);
    verify(padPipelineSubmissionRepository, never()).saveAll(any());
  }

  @Test
  void assignPipelineReferences_canShow_withPipeline_noTemporaryRefSet() {
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.CABLE);
    when(applicationTaskService.canShowTask(APP_TASK, detail)).thenReturn(true);
    when(padPipelineSubmissionRepository.getNonConsentedPipelines(detail)).thenReturn(List.of(padPipeline));

    padPipelineNumberingService.assignPipelineReferences(detail);
    verify(padPipelineSubmissionRepository, times(1)).saveAll(List.of(padPipeline));
  }

  @Test
  void assignPipelineReferences_canShow_withPipeline_temporaryRefSet() {
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.CABLE);
    padPipeline.setTemporaryRef("temp ref");
    when(applicationTaskService.canShowTask(APP_TASK, detail)).thenReturn(true);
    when(padPipelineSubmissionRepository.getNonConsentedPipelines(detail)).thenReturn(List.of(padPipeline));

    padPipelineNumberingService.assignPipelineReferences(detail);
    verify(padPipelineSubmissionRepository, times(0)).saveAll(any());
  }

  @Test
  void assignPipelineReferences_cannotShow() {
    when(applicationTaskService.canShowTask(APP_TASK, detail)).thenReturn(false);
    padPipelineNumberingService.assignPipelineReferences(detail);
    verify(padPipelineSubmissionRepository, never()).saveAll(any());
  }

  @Test
  void attachNewReference_singleCore() {
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.CABLE);
    when(padPipelineSubmissionRepository.getNextPipelineReferenceNumber()).thenReturn(1L);

    padPipelineNumberingService.attachNewReference(padPipeline);
    assertThat(padPipeline.getPipelineRef()).isEqualTo(PipelineCoreType.SINGLE_CORE.getReferencePrefix() + "1");
  }

  @Test
  void attachNewReference_multiCore() {
    var padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.UMBILICAL_JUMPER);
    when(padPipelineSubmissionRepository.getNextPipelineReferenceNumber()).thenReturn(1L);

    padPipelineNumberingService.attachNewReference(padPipeline);
    assertThat(padPipeline.getPipelineRef()).isEqualTo(PipelineCoreType.MULTI_CORE.getReferencePrefix() + "1");
  }

  @Test
  void nonConsentedPadPipelineRequiresFullReference_whenHasTemporaryRef() {
    var padPipeline = new PadPipeline();
    padPipeline.setTemporaryRef("something");
    assertThat(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(padPipeline)).isFalse();

  }

  @Test
  void nonConsentedPadPipelineRequiresFullReference_whenHasNoTemporaryRef() {
    var padPipeline = new PadPipeline();

    assertThat(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(padPipeline)).isTrue();

  }

  @Test
  void setManualPipelineReference_temporaryRefAlreadySet() {

    var padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("123");
    padPipeline.setTemporaryRef("abc");
    padPipelineNumberingService.setManualPipelineReference(padPipeline, "xyz");

    assertThat(padPipeline.getPipelineRef()).isEqualTo("xyz");
    assertThat(padPipeline.getTemporaryRef()).isEqualTo("abc");

  }

  @Test
  void setManualPipelineReference_temporaryNotSet() {

    var padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("123");
    padPipelineNumberingService.setManualPipelineReference(padPipeline, "xyz");

    assertThat(padPipeline.getPipelineRef()).isEqualTo("xyz");
    assertThat(padPipeline.getTemporaryRef()).isEqualTo("123");

  }
}