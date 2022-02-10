package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineServiceTest {

  @Mock
  private PipelineRepository pipelineRepository;

  @Mock
  private PipelineDetailRepository pipelineDetailRepository;

  private PipelineService pipelineService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setup() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pipelineService = new PipelineService(pipelineRepository);

    when(pipelineRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
  }


  @Test
  public void createApplicationPipeline_associatesPipelineWithAppPwa() {

    var newPipeline = pipelineService.createApplicationPipeline(pwaApplicationDetail.getPwaApplication());
    assertThat(newPipeline.getMasterPwa()).isEqualTo(pwaApplicationDetail.getPwaApplication().getMasterPwa());
  }

  @Test
  public void getPipelinesFromIds_serviceInteraction(){
    var ids = Set.of(new PipelineId(1), new PipelineId(2));
    pipelineService.getPipelinesFromIds(ids);
    verify(pipelineRepository, times(1)).findAllById(Set.of(1,2));

  }

}