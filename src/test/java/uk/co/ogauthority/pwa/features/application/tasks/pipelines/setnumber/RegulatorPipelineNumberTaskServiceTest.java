package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist.PadPipelineTaskListHeader;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegulatorPipelineNumberTaskServiceTest {

  @Mock
  private PadPipelineNumberingService padPipelineNumberingService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private SetPipelineNumberFormValidator setPipelineNumberFormValidator;

  @Mock
  private PipelineMigrationConfigRepository pipelineMigrationConfigRepository;

  private RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService;

  private PadPipeline padPipeline;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {

    regulatorPipelineNumberTaskService = new RegulatorPipelineNumberTaskService(
        padPipelineNumberingService,
        pipelineDetailService,
        setPipelineNumberFormValidator,
        pipelineMigrationConfigRepository);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    padPipeline.setId(2);


    when(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(any())).thenReturn(true);

  }

  @Test
  void pipelineTaskAccessible_validPermissions_pipelineConsented() {
    when(pipelineDetailService.isPipelineConsented(padPipeline.getPipeline())).thenReturn(true);

    assertThat(regulatorPipelineNumberTaskService.pipelineTaskAccessible(
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE), padPipeline))
        .isFalse();
  }

  @Test
  void pipelineTaskAccessible_validPermissions_pipelineNotConsented() {

    assertThat(regulatorPipelineNumberTaskService.pipelineTaskAccessible(
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE), padPipeline))
        .isTrue();
  }

  @Test
  void pipelineTaskAccessible_invalidPermissions_pipelineNotConsented() {

    assertThat(regulatorPipelineNumberTaskService.pipelineTaskAccessible(Set.of(PwaApplicationPermission.EDIT),
        padPipeline))
        .isFalse();
  }

  @Test
  void getTaskListEntry_whenValidPipelineAndUser_andFullReferenceDefined() {
    var context = new PwaApplicationContext(pwaApplicationDetail, null,
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE));
    var taskListHeader = new PadPipelineTaskListHeader(padPipeline, 0, PipelineStatus.IN_SERVICE, "Some name",
        false);

    when(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(any())).thenReturn(false);

    assertThat(regulatorPipelineNumberTaskService.getTaskListEntry(context, taskListHeader)).isPresent()
        .hasValueSatisfying(taskListEntry -> {
          assertThat(taskListEntry.getTaskName()).containsIgnoringCase("set pipeline number");
          assertThat(taskListEntry.isCompleted()).isTrue();
          assertThat(taskListEntry.getDisplayOrder()).isEqualTo(5);
          assertThat(taskListEntry.getRoute()).isNotNull();
        });
  }

  @Test
  void getTaskListEntry_whenValidPipelineAndUser_andFullReferenceNotDefined() {

    var context = new PwaApplicationContext(pwaApplicationDetail, null,
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE));
    var taskListHeader = new PadPipelineTaskListHeader(padPipeline, 0, PipelineStatus.IN_SERVICE, "Some name",
        false);

    when(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(any())).thenReturn(true);

    assertThat(regulatorPipelineNumberTaskService.getTaskListEntry(context, taskListHeader)).isPresent()
        .hasValueSatisfying(taskListEntry ->
          assertThat(taskListEntry.isCompleted()).isFalse());

  }

  @Test
  void getPermittedPipelineNumberRange_serviceInteractions(){
    var config = PipelineMigrationConfigTestUtil.create(5000, 6000);

    when(pipelineMigrationConfigRepository.findAll()).thenReturn(IterableUtil.iterable(config));

    assertThat(regulatorPipelineNumberTaskService.getPermittedPipelineNumberRange()).satisfies(integerRange -> {
      assertThat(integerRange.getMinimum()).isEqualTo(5000);
      assertThat(integerRange.getMaximum()).isEqualTo(6000);

    });


  }

  @Test
  void getPermittedPipelineNumberRange_configNotFound(){
    assertThrows(PipelineNumberConfigException.class, () ->
      regulatorPipelineNumberTaskService.getPermittedPipelineNumberRange());


  }

}