package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.ModifyPipelineForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
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

  @Before
  public void setUp() {
    modifyPipelineService = new ModifyPipelineService(
        padPipelineService,
        pipelineDetailService,
        pipelineDetailIdentDataImportService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_consentedPipelineAvailable() {

    // TODO PWA-1047 rename these variables to "consentedPipeline" or "consentedPipelineNotOnApp".
    //  (if a pipelineDetail exists it is already assumed to be consented)
    // Also, extract this setup code as this entire test is called a setup step for other tests. which is bad.
    var nonConsentedPipeline = new Pipeline(detail.getPwaApplication());
    nonConsentedPipeline.setId(3);

    var nonConsentedPadPipeline = new PadPipeline();
    nonConsentedPadPipeline.setPipeline(nonConsentedPipeline);
    nonConsentedPadPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    nonConsentedPadPipeline.setPipelineRef("Pipeline ref");

    var nonConsentedPipelineDetail = new PipelineDetail();
    nonConsentedPipelineDetail.setPipelineStatus(PipelineStatus.IN_SERVICE);
    nonConsentedPipelineDetail.setPipeline(nonConsentedPipeline);

    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(nonConsentedPipelineDetail));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).containsExactly(nonConsentedPipelineDetail);
  }

  @Test
  public void getConsentedPipelinesNotOnApplication_consentedPipelineAlreadyLinked() {

    var consentedPipeline = new Pipeline(detail.getPwaApplication());
    consentedPipeline.setId(3);

    var consentedPipelineDetail = new PipelineDetail();
    consentedPipelineDetail.setPipeline(consentedPipeline);

    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(consentedPipelineDetail));

    var padPipeline = new PadPipeline();

    padPipeline.setPipeline(consentedPipeline);
    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(padPipeline));

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

    // TODO PWA-1047 rename this variable to "consentedPipeline" or "consentedPipelineNotOnApp"
    // Also, extract this setup code as this entire test is called a setup step for other tests. which is bad.
    var nonConsentedPipeline = new Pipeline(detail.getPwaApplication());
    nonConsentedPipeline.setId(3);

    var nonConsentedPipelineDetail = new PipelineDetail();
    nonConsentedPipelineDetail.setPipelineStatus(PipelineStatus.IN_SERVICE);
    nonConsentedPipelineDetail.setPipeline(nonConsentedPipeline);

    when(pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(detail.getMasterPwa()))
        .thenReturn(List.of(nonConsentedPipelineDetail));

    var result = modifyPipelineService.getConsentedPipelinesNotOnApplication(detail);

    assertThat(result).containsExactly(nonConsentedPipelineDetail);
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
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);

    var pipelineOverview = new PadPipelineOverview(padPipeline, 1L);

    when(padPipelineService.getPipelines(detail)).thenReturn(List.of());

    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).extracting(NamedPipeline::getPipelineId)
        .containsExactly(3);
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

    var result = modifyPipelineService.getSelectableConsentedPipelines(detail);
    assertThat(result).extracting(NamedPipeline::getPipelineId)
        .containsExactly(3);
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