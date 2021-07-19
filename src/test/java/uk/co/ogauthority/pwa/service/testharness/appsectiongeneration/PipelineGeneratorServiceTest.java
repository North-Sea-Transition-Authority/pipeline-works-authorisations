package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelinePersisterService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.pipelinegenerator.PipelineGeneratorService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineGeneratorServiceTest {


  @Mock
  private PipelineService pipelineService;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PadPipelinePersisterService padPipelinePersisterService;

  @Mock
  private PadPipelineIdentRepository padPipelineIdentRepository;

  @Mock
  private PadPipelineIdentDataRepository padPipelineIdentDataRepository;

  PipelineGeneratorService pipelineGeneratorService;

  @Before
  public void setup(){
    pipelineGeneratorService = new PipelineGeneratorService(
        pipelineService, padPipelineRepository, padPipelinePersisterService, padPipelineIdentRepository, padPipelineIdentDataRepository);
  }

  @Test
  public void generatePadPipelinesAndIdents()  {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);

    var pipelineQuantity = 5;
    pipelineGeneratorService.generatePadPipelinesAndIdents(pwaApplicationDetail, pipelineQuantity);

    verify(padPipelinePersisterService, times(2 * pipelineQuantity))
        .savePadPipelineAndMaterialiseIdentData(any(PadPipeline.class));
    verify(padPipelineIdentRepository, times(pipelineQuantity)).saveAll(anyList());
    verify(padPipelineIdentDataRepository, times(pipelineQuantity)).saveAll(anyList());

  }


  
  
}
