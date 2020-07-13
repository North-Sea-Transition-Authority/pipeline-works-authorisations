package uk.co.ogauthority.pwa.service.pwaconsents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

}