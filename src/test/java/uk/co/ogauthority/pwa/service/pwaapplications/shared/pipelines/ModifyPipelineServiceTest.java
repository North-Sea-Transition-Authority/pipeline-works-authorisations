package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ModifyPipelineServiceTest {

  @Mock
  private PipelineService pipelineService;

  @Mock
  private PadPipelineService padPipelineService;

  private ModifyPipelineService modifyPipelineService;

  private PwaApplicationDetail detail;

  @Before
  public void setUp() {
    modifyPipelineService = new ModifyPipelineService(pipelineService, padPipelineService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_consentedPipelineAvailable() {
    when(padPipelineService.getMasterPipelineIds(detail)).thenReturn(List.of(1, 2));

    var nonConsentedPipeline = new Pipeline(detail.getPwaApplication());
    nonConsentedPipeline.setId(3);

    var nonConsentedPadPipeline = new PadPipeline();
    nonConsentedPadPipeline.setPipeline(nonConsentedPipeline);
    nonConsentedPadPipeline.setPipelineRef("Pipeline ref");

    var nonConsentedPipelineDetail = new PipelineDetail();
    nonConsentedPipelineDetail.setPipeline(nonConsentedPipeline);

    when(pipelineService.getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag(detail.getPwaApplication(),
        true))
        .thenReturn(List.of(nonConsentedPipelineDetail));

    when(padPipelineService.getPadPipelinesByMasterAndIds(detail.getMasterPwaApplication(), List.of(3)))
        .thenReturn(List.of(nonConsentedPadPipeline));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).containsExactly(nonConsentedPadPipeline);
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_consentedPipelineAlreadyLinked() {
    when(padPipelineService.getMasterPipelineIds(detail)).thenReturn(List.of(1, 2));

    var nonConsentedPipeline = new Pipeline(detail.getPwaApplication());
    nonConsentedPipeline.setId(3);

    var nonConsentedPadPipeline = new PadPipeline();
    nonConsentedPadPipeline.setPipeline(nonConsentedPipeline);
    nonConsentedPadPipeline.setPipelineRef("Pipeline ref");

    var nonConsentedPipelineDetail = new PipelineDetail();
    nonConsentedPipelineDetail.setPipeline(nonConsentedPipeline);

    when(pipelineService.getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag(detail.getPwaApplication(),
        true))
        .thenReturn(List.of(nonConsentedPipelineDetail));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).isEmpty();
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_noConsentedPipelines() {
    when(padPipelineService.getMasterPipelineIds(detail)).thenReturn(List.of(1, 2));

    when(pipelineService.getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag(detail.getPwaApplication(),
        true))
        .thenReturn(List.of());

    when(padPipelineService.getPadPipelinesByMasterAndIds(detail.getMasterPwaApplication(), List.of()))
        .thenReturn(List.of());

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).isEmpty();
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_noPipelinesOnAppDetail() {
    when(padPipelineService.getMasterPipelineIds(detail)).thenReturn(List.of());

    var nonConsentedPipeline = new Pipeline(detail.getPwaApplication());
    nonConsentedPipeline.setId(3);

    var nonConsentedPadPipeline = new PadPipeline();
    nonConsentedPadPipeline.setPipeline(nonConsentedPipeline);
    nonConsentedPadPipeline.setPipelineRef("Pipeline ref");

    var nonConsentedPipelineDetail = new PipelineDetail();
    nonConsentedPipelineDetail.setPipeline(nonConsentedPipeline);

    when(pipelineService.getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag(detail.getPwaApplication(),
        true))
        .thenReturn(List.of(nonConsentedPipelineDetail));

    when(padPipelineService.getPadPipelinesByMasterAndIds(detail.getMasterPwaApplication(), List.of(3)))
        .thenReturn(List.of(nonConsentedPadPipeline));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).containsExactly(nonConsentedPadPipeline);
  }

  @Test
  public void getSelectableConsentedPipelines_consentedPipelineAvailable() {
    // Perform setup from previous test as underlying calls rely on this method.
    getConsentedPipelinesNotOnApplication_consentedPipelineAvailable();

    var padPipeline = new PadPipeline();
    var pipeline = new Pipeline();
    pipeline.setId(3);
    padPipeline.setId(1);
    padPipeline.setPipeline(pipeline);
    padPipeline.setPipelineType(PipelineType.GAS_LIFT_PIPELINE);

    var pipelineOverview = new PadPipelineOverview(padPipeline, 1L);

    when(padPipelineService.getPipelineOverviews(any()))
        .thenReturn(List.of(pipelineOverview));

    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).containsOnlyKeys("3");
  }

  @Test
  public void getSelectableConsentedPipelines_noPipelinesOnAppDetail() {
    // Perform setup from previous test as underlying calls rely on this method.
    getConsentedPipelinesNotOnApplication_noPipelinesOnAppDetail();

    var padPipeline = new PadPipeline();
    var pipeline = new Pipeline();
    pipeline.setId(3);
    padPipeline.setId(1);
    padPipeline.setPipeline(pipeline);
    padPipeline.setPipelineType(PipelineType.GAS_LIFT_PIPELINE);

    var pipelineOverview = new PadPipelineOverview(padPipeline, 1L);

    when(padPipelineService.getPipelineOverviews(any()))
        .thenReturn(List.of(pipelineOverview));

    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).containsOnlyKeys("3");
  }

  @Test
  public void getSelectableConsentedPipelines_noConsentedPipelines() {
    // Perform setup from previous test as underlying calls rely on this method.
    getConsentedPipelinesNotOnApplication_noConsentedPipelines();
    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).isEmpty();
  }

  @Test
  public void getSelectableConsentedPipelines_consentedPipelineAlreadyLinked() {
    // Perform setup from previous test as underlying calls rely on this method.
    getConsentedPipelinesNotOnApplication_consentedPipelineAlreadyLinked();
    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).isEmpty();
  }

  @Test
  public void importPipeline_serviceInteraction() {
    var form = new ModifyPipelineForm();
    form.setPipelineId("1");
    var pipeline = new PadPipeline();
    when(padPipelineService.getPadPipelinesByMasterAndId(detail.getMasterPwaApplication(), 1))
        .thenReturn(pipeline);
    modifyPipelineService.importPipeline(detail, form);
    verify(padPipelineService, times(1)).copyDataToNewPadPipeline(detail, pipeline);
  }
}