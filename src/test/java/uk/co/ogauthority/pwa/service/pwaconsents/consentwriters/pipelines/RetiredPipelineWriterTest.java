package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineRemovalService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@ExtendWith(MockitoExtension.class)
class RetiredPipelineWriterTest {

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PipelineRemovalService pipelineRemovalService;

  private RetiredPipelineWriter linkWriter;

  private PwaApplicationDetail pwaApplicationDetail;

  private PwaConsent pwaConsent;

  private ConsentWriterDto consentWriterDto;

  private Map<Pipeline, PipelineDetail> pipelineDetailMap;

  @Captor
  private ArgumentCaptor<List<Pipeline>> pipelineCaptor;
  private PadPipeline padPipeline;

  @BeforeEach
  void setup() {
    linkWriter = new RetiredPipelineWriter(padOrganisationRoleService, padPipelineService, pipelineRemovalService);

    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setStatus(PwaApplicationStatus.DRAFT);
    pwaConsent = new PwaConsent();
    consentWriterDto = new ConsentWriterDto();
    pipelineDetailMap = getPipelineMap();
    consentWriterDto.setPipelineToNewDetailMap(pipelineDetailMap);

    padPipeline = new PadPipeline();
    padPipeline.setPwaApplicationDetail(pwaApplicationDetail);
  }

  @Test
  void linkWriter_noPipelinesModifiedInApplication() {
    consentWriterDto.setPipelineToNewDetailMap(Collections.emptyMap());
    linkWriter.write(pwaApplicationDetail, pwaConsent, consentWriterDto);
    verify(padOrganisationRoleService, never()).removePipelineLinksForRetiredPipelines(any());
    verify(pipelineRemovalService, never()).removePipeline(any());
  }

  @Test
  void linkWriter_pipelinesInApplicationsToRemove() {
    when(padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(any())).thenReturn(List.of(padPipeline));
    linkWriter.write(pwaApplicationDetail, pwaConsent, consentWriterDto);
    verify(padOrganisationRoleService, times(1)).removePipelineLinksForRetiredPipelines(pipelineCaptor.capture());
    verify(padPipelineService, times(PipelineStatus.getStatusesWithoutState(PhysicalPipelineState.ON_SEABED).size())).findSubmittedOrDraftPipelinesWithPipelineNumber(any());

    var pipelines = pipelineCaptor.getValue();
    assertThat(pipelines).hasSize(6);
    for (var pipeline : pipelines) {
      var pipelineDetail = pipelineDetailMap.get(pipeline);
      assertThat(pipelineDetail.getPipelineStatus()).isNotIn(PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED));
    }
  }

  @Test
  void linkWriter_pipelinesInSubmittedApplications() {
    when(padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(any())).thenReturn(List.of(padPipeline));
    var padPipeline = new PadPipeline();
    var pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    padPipeline.setPwaApplicationDetail(pwaApplicationDetail);
    when(padPipelineService.findSubmittedOrDraftPipelinesWithPipelineNumber(any())).thenReturn(List.of(padPipeline));

    linkWriter.write(pwaApplicationDetail, pwaConsent, consentWriterDto);
    verify(padOrganisationRoleService, times(1)).removePipelineLinksForRetiredPipelines(any());
    verify(pipelineRemovalService, never()).removePipeline(any());
  }

  private Map<Pipeline, PipelineDetail> getPipelineMap() {
    var plNumber = 1;
    var map = new HashMap<Pipeline, PipelineDetail>();
    for (var status : PipelineStatus.values()) {
      var pipeline = new Pipeline();
      pipeline.setId(plNumber);
      plNumber++;
      var pipelineDetail = new PipelineDetail();
      pipelineDetail.setPipelineStatus(status);
      map.put(pipeline, pipelineDetail);
    }
    return map;
  }
}
