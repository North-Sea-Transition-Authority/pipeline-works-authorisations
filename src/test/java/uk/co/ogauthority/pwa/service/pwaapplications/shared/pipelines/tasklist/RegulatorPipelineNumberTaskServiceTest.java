package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.submission.PadPipelineNumberingService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class RegulatorPipelineNumberTaskServiceTest {

  @Mock
  private PadPipelineNumberingService padPipelineNumberingService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private SetPipelineNumberFormValidator setPipelineNumberFormValidator;

  private RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService;

  private PadPipeline padPipeline;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {

    regulatorPipelineNumberTaskService = new RegulatorPipelineNumberTaskService(
        padPipelineNumberingService,
        pipelineDetailService,
        setPipelineNumberFormValidator
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    padPipeline.setId(2);


    when(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(any())).thenReturn(true);

  }

  @Test
  public void pipelineTaskAccessible_validPermissions_pipelineConsented() {
    when(pipelineDetailService.isPipelineConsented(padPipeline.getPipeline())).thenReturn(true);

    assertThat(regulatorPipelineNumberTaskService.pipelineTaskAccessible(
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE), padPipeline))
        .isFalse();
  }

  @Test
  public void pipelineTaskAccessible_validPermissions_pipelineNotConsented() {

    assertThat(regulatorPipelineNumberTaskService.pipelineTaskAccessible(
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE), padPipeline))
        .isTrue();
  }

  @Test
  public void pipelineTaskAccessible_invalidPermissions_pipelineNotConsented() {

    assertThat(regulatorPipelineNumberTaskService.pipelineTaskAccessible(Set.of(PwaApplicationPermission.EDIT),
        padPipeline))
        .isFalse();
  }

  @Test
  public void getTaskListEntry_whenValidPipelineAndUser_andFullReferenceDefined() {
    var context = new PwaApplicationContext(pwaApplicationDetail, null,
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE));
    var taskListHeader = new PadPipelineTaskListHeader(padPipeline, 0, PipelineStatus.IN_SERVICE, "Some name");

    when(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(any())).thenReturn(false);

    assertThat(regulatorPipelineNumberTaskService.getTaskListEntry(context, taskListHeader)).isPresent()
        .hasValueSatisfying(taskListEntry -> {
          assertThat(taskListEntry.getTaskName()).containsIgnoringCase("set pipeline reference");
          assertThat(taskListEntry.isCompleted()).isTrue();
          assertThat(taskListEntry.getDisplayOrder()).isEqualTo(5);
          assertThat(taskListEntry.getRoute()).isNotNull();

        });

  }

  @Test
  public void getTaskListEntry_whenValidPipelineAndUser_andFullReferenceNotDefined() {

    var context = new PwaApplicationContext(pwaApplicationDetail, null,
        Set.of(PwaApplicationPermission.SET_PIPELINE_REFERENCE));
    var taskListHeader = new PadPipelineTaskListHeader(padPipeline, 0, PipelineStatus.IN_SERVICE, "Some name");

    when(padPipelineNumberingService.nonConsentedPadPipelineRequiresFullReference(any())).thenReturn(true);

    assertThat(regulatorPipelineNumberTaskService.getTaskListEntry(context, taskListHeader)).isPresent()
        .hasValueSatisfying(taskListEntry -> {
          assertThat(taskListEntry.isCompleted()).isFalse();
        });

  }
}