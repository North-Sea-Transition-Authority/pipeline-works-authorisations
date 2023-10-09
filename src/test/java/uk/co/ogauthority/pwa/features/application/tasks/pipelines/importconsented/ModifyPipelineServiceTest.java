package uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.NamedPipeline;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.NamedPipelineDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentDataImportService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ModifyPipelineServiceTest {

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PipelineDetailIdentDataImportService pipelineDetailIdentDataImportService;

  private ModifyPipelineService modifyPipelineService;

  private PwaApplicationDetail detail;

  private Pipeline consentedPipeline;

  private PipelineDetail consentedPipelineDetail;

  private PadPipeline consentedPadPipeline;

  @Before
  public void setUp() {
    modifyPipelineService = new ModifyPipelineService(
        padPipelineService,
        pipelineDetailService,
        pipelineDetailIdentDataImportService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    consentedPipeline = new Pipeline(detail.getPwaApplication());
    consentedPipeline.setId(3);

    consentedPipelineDetail = new PipelineDetail();
    consentedPipelineDetail.setPipeline(consentedPipeline);
    consentedPipelineDetail.setPipelineStatus(PipelineStatus.IN_SERVICE);
    consentedPipelineDetail.setPipelineType(PipelineType.GAS_LIFT_PIPELINE);

    consentedPadPipeline = new PadPipeline();
    consentedPadPipeline.setPipeline(consentedPipeline);
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_consentedPipelineAvailable() {
    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(consentedPipelineDetail));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).containsExactly(consentedPipelineDetail);
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_consentedPipelineAlreadyLinked() {
    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(consentedPipelineDetail));

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(consentedPadPipeline));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).isEmpty();
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_noConsentedPipelines() {
    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).isEmpty();
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_noPipelinesOnAppDetail() {
    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(consentedPipelineDetail));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).containsExactly(consentedPipelineDetail);
  }

  @Test
  public void getSelectableConsentedPipelines_consentedPipelineAvailable() {
    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(consentedPipelineDetail));

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of());

    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).extracting(NamedPipeline::getPipelineId, NamedPipelineDto::getPipelineType)
        .containsExactly(Tuple.tuple(3, PipelineType.GAS_LIFT_PIPELINE));
  }

  @Test
  public void getSelectableConsentedPipelines_consentedReturnedToShorePipelineAvailable() {
    var rtsPipeline = new Pipeline(detail.getPwaApplication());
    rtsPipeline.setId(1);

    var rtsPipelineDetail = new PipelineDetail();
    rtsPipelineDetail.setPipeline(rtsPipeline);
    rtsPipelineDetail.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);
    rtsPipelineDetail.setPipelineType(PipelineType.METHANOL_PIPELINE);

    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(rtsPipelineDetail, consentedPipelineDetail));

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of());

    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).extracting(NamedPipeline::getPipelineId, NamedPipelineDto::getPipelineType)
        .containsExactly(
            Tuple.tuple(3, PipelineType.GAS_LIFT_PIPELINE),
            Tuple.tuple(1, PipelineType.METHANOL_PIPELINE)
        );
  }

  @Test
  public void getSelectableConsentedPipelines_noConsentedPipelines() {
    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).isEmpty();
  }

  @Test
  public void getSelectableConsentedPipelines_consentedPipelineAlreadyLinked() {
    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(consentedPipelineDetail));

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(consentedPadPipeline));

    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).isEmpty();
  }

  @Test
  public void importPipeline_serviceInteraction() {
    var form = new ModifyPipelineForm();
    form.setPipelineId("1");
    var pipelineDetail = new PipelineDetail();

    when(pipelineDetailService.getLatestByPipelineId(1)).thenReturn(pipelineDetail);
    modifyPipelineService.importPipeline(detail, form);
    verify(padPipelineService, times(1)).copyDataToNewPadPipeline(detail, pipelineDetail, form);
    verify(pipelineDetailIdentDataImportService, times(1)).importIdentsAndData(eq(pipelineDetail), any());
  }

  @Test(expected = ActionNotAllowedException.class)
  public void importPipeline_modifyingTransferredPipeline_errorThrown() {
    var form = new ModifyPipelineForm();
    form.setPipelineId("1");
    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setPipelineStatus(PipelineStatus.TRANSFERRED);

    when(pipelineDetailService.getLatestByPipelineId(1)).thenReturn(pipelineDetail);
    modifyPipelineService.importPipeline(detail, form);
  }

  @Test
  public void getPipelineServiceStatusesForAppType_validAppTypesForTransferredPipelineStatus() {

    var validAppTypesForTransferredPipelineStatus = List.of(
        PwaApplicationType.CAT_1_VARIATION, PwaApplicationType.CAT_2_VARIATION, PwaApplicationType.DECOMMISSIONING);

    validAppTypesForTransferredPipelineStatus.forEach(appType -> {
      var actualPipelineStatuses = modifyPipelineService.getPipelineServiceStatusesForAppType(appType);
      assertThat(actualPipelineStatuses).isEqualTo(PipelineStatus.toOrderedListWithoutHistorical());
    });
  }

  @Test
  public void getPipelineServiceStatusesForAppType_nonValidAppTypesForTransferredPipelineStatus() {

    var actualPipelineStatuses = modifyPipelineService.getPipelineServiceStatusesForAppType(PwaApplicationType.OPTIONS_VARIATION);
    var expectedPipelineStatuses = PipelineStatus.toOrderedListWithoutHistorical().stream()
        .filter(pipelineStatus -> !pipelineStatus.equals(PipelineStatus.TRANSFERRED))
        .collect(Collectors.toList());

    assertThat(actualPipelineStatuses).isEqualTo(expectedPipelineStatuses);
  }

}