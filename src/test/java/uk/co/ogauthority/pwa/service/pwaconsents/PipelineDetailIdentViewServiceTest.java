package uk.co.ogauthority.pwa.service.pwaconsents;


import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineIdentViewCollectorService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentViewService;

@ExtendWith(MockitoExtension.class)
class PipelineDetailIdentViewServiceTest {

  private final PipelineId PIPELINE_ID = new PipelineId(1);

  @Mock
  private PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;

  @Mock
  private PipelineDetailIdentRepository pipelineDetailIdentRepository;

  // do not mock so we use real code
  private PipelineIdentViewCollectorService pipelineIdentViewCollectorService;

  private PipelineDetailIdentViewService pipelineDetailIdentViewService;

  @BeforeEach
  void setUp() throws Exception {

    pipelineIdentViewCollectorService = new PipelineIdentViewCollectorService();

    pipelineDetailIdentViewService = new PipelineDetailIdentViewService(
        pipelineDetailIdentDataRepository,
        pipelineDetailIdentRepository,
        pipelineIdentViewCollectorService);
  }

  @Test
  void getSortedPipelineIdentViewsForPipeline_sortsIdentsByNumber() {

    var identData = List.of(
        createPipelineIdentData(PIPELINE_ID, 2),
        createPipelineIdentData(PIPELINE_ID, 1)
    );

    var idents = identData.stream()
        .map(PipelineDetailIdentData::getPipelineDetailIdent)
        .collect(toList());

    when(pipelineDetailIdentRepository.findByPipelineDetail_Pipeline_IdInAndPipelineDetail_tipFlagIsTrue(
        Set.of(PIPELINE_ID.asInt()))
    ).thenReturn(idents);

    when(pipelineDetailIdentDataRepository.getAllByPipelineDetailIdentIn(any())).thenReturn(identData);

    var identViews = pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipeline(PIPELINE_ID);

    assertThat(identViews).hasSize(2);

    assertThat(identViews.get(0).getIdentNumber()).isEqualTo(1);
    assertThat(identViews.get(1).getIdentNumber()).isEqualTo(2);

  }

  @Test
  void getSortedPipelineIdentViewsForPipelineDetail_sortsIdentsByNumber() {

    var identData = List.of(
        createPipelineIdentData(PIPELINE_ID, 2),
        createPipelineIdentData(PIPELINE_ID, 1)
    );

    var idents = identData.stream()
        .map(PipelineDetailIdentData::getPipelineDetailIdent)
        .collect(toList());

    var pipelineDetailId = 1;

    when(pipelineDetailIdentRepository.findAllByPipelineDetail_id(pipelineDetailId)).thenReturn(idents);

    when(pipelineDetailIdentDataRepository.getAllByPipelineDetailIdentIn(any())).thenReturn(identData);

    var identViews = pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelineDetail(PIPELINE_ID, pipelineDetailId);

    assertThat(identViews).hasSize(2);

    assertThat(identViews.get(0).getIdentNumber()).isEqualTo(1);
    assertThat(identViews.get(1).getIdentNumber()).isEqualTo(2);

  }

  private PipelineDetailIdentData createPipelineIdentData(PipelineId pipelineId, int identNumber) {
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId.asInt());
    var pipelineDetail = new PipelineDetail(pipeline);

    var ident = new PipelineDetailIdent();
    ident.setPipelineDetail(pipelineDetail);
    ident.setIdentNo(identNumber);

    var identData = new PipelineDetailIdentData();
    identData.setPipelineDetailIdent(ident);

    return identData;
  }
}