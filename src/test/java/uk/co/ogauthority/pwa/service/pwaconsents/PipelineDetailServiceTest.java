package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDetailServiceTest {

  @Mock
  private PipelineDetailRepository pipelineDetailRepository;

  private PipelineDetailService pipelineDetailService;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() {
    pipelineDetailService = new PipelineDetailService(pipelineDetailRepository);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void getSimilarPipelineBundleNamesByDetailAndNameLike_serviceInteraction() {
    when(pipelineDetailRepository.getBundleNamesByPwaApplicationDetail(detail)).thenReturn(List.of());
    var result = pipelineDetailService.getSimilarPipelineBundleNamesByDetail(detail);
    assertThat(result).isEqualTo(List.of());
  }

  @Test
  public void getNonDeletedPipelineDetailsForApplicationMasterPwaWithTipFlag_serviceInteraction() {
    var master = detail.getMasterPwa();
    var pipelineDetail = new PipelineDetail();
    when(pipelineDetailRepository.findAllByPipeline_MasterPwaAndPipelineStatusIsNotInAndTipFlagIsTrue(master, PipelineStatus.historicalStatusSet()))        .thenReturn(List.of(pipelineDetail));
    var result = pipelineDetailService.getNonDeletedPipelineDetailsForApplicationMasterPwa(master);
    assertThat(result).containsExactly(pipelineDetail);
  }

  @Test
  public void getActivePipelineDetailsForApplicationMasterPwa_serviceInteraction() {
    pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwa(detail.getPwaApplication());
    verify(pipelineDetailRepository, times(1)).findAllByPipeline_MasterPwaAndEndTimestampIsNull(
        detail.getPwaApplication().getMasterPwa());
  }

  @Test
  public void isPipelineConsented_consented() {
    var pipelineDetail = new PipelineDetail();
    var pipeline = new Pipeline();
    pipeline.setId(1);
    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId())).thenReturn(Optional.of(pipelineDetail));

    assertThat(pipelineDetailService.isPipelineConsented(pipeline)).isTrue();
  }

  @Test
  public void isPipelineConsented_notConsented() {
    var pipeline = new Pipeline();
    pipeline.setId(1);
    when(pipelineDetailRepository.getByPipeline_IdAndTipFlagIsTrue(pipeline.getId())).thenReturn(Optional.empty());

    assertThat(pipelineDetailService.isPipelineConsented(pipeline)).isFalse();
  }

}