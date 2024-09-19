package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PadPipelineDto;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PipelineWriterTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDetailIdentServiceTest {

  @Mock
  private PipelineDetailIdentRepository pipelineDetailIdentRepository;

  @Mock
  private PipelineIdentMappingService pipelineIdentMappingService;

  @Mock
  private PipelineDetailIdentDataService pipelineDetailIdentDataService;

  private PipelineDetailIdentService pipelineDetailIdentService;

  @Captor
  private ArgumentCaptor<Collection<PipelineDetailIdent>> pipelineDetailIdentsCaptor;

  @Captor
  private ArgumentCaptor<Map<PipelineDetailIdent, Set<PadPipelineIdentData>>> identDataCreationMapCaptor;

  private Map<PipelineDetail, PadPipelineDto> pipelineDetailToPadPipelineDtoMap;

  @Before
  public void setUp() throws Exception {

    pipelineDetailToPadPipelineDtoMap = PipelineWriterTestUtils.createPipelineDetailToPadPipelineDtoMap();
    pipelineDetailIdentService = new PipelineDetailIdentService(pipelineDetailIdentRepository, pipelineIdentMappingService, pipelineDetailIdentDataService);

  }

  @Test
  public void createPipelineDetailIdents() {

    pipelineDetailIdentService.createPipelineDetailIdents(pipelineDetailToPadPipelineDtoMap);

    verify(pipelineDetailIdentRepository, times(1)).saveAll(pipelineDetailIdentsCaptor.capture());

    var newIdents = new ArrayList<>(pipelineDetailIdentsCaptor.getValue());

    assertThat(newIdents).allSatisfy(ident -> assertThat(pipelineDetailToPadPipelineDtoMap).containsKey(ident.getPipelineDetail()));

    verify(pipelineDetailIdentDataService, times(1)).createPipelineDetailIdentData(identDataCreationMapCaptor.capture());

    assertThat(identDataCreationMapCaptor.getValue().keySet()).containsAll(newIdents);

    identDataCreationMapCaptor.getValue().forEach((newIdent, dataSet) -> {

      verify(pipelineIdentMappingService, times(1)).mapIdent(newIdent, dataSet.iterator().next().getPadPipelineIdent());

    });

  }

}