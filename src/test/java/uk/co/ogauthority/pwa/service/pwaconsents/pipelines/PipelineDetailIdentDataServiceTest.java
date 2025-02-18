package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PipelineWriterTestUtils;

@ExtendWith(MockitoExtension.class)
class PipelineDetailIdentDataServiceTest {

  @Mock
  private PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;

  @Mock
  private PipelineIdentDataMappingService pipelineIdentDataMappingService;

  @Captor
  private ArgumentCaptor<List<PipelineDetailIdentData>> identDataCaptor;

  private PipelineDetailIdentDataService pipelineDetailIdentDataService;

  private Map<PipelineDetailIdent, Set<PadPipelineIdentData>> pipelineIdentToDataSetMap;

  @BeforeEach
  void setUp() throws Exception {

    pipelineIdentToDataSetMap = PipelineWriterTestUtils.createIdentToDataSetMap();

    pipelineDetailIdentDataService = new PipelineDetailIdentDataService(pipelineDetailIdentDataRepository, pipelineIdentDataMappingService);

  }

  @Test
  void createPipelineDetailIdentData() {

    pipelineDetailIdentDataService.createPipelineDetailIdentData(pipelineIdentToDataSetMap);

    verify(pipelineDetailIdentDataRepository, times(1)).saveAll(identDataCaptor.capture());

    var identDataList = new ArrayList<>(identDataCaptor.getValue());

    assertThat(identDataList).allSatisfy(identData ->
        assertThat(pipelineIdentToDataSetMap).containsKey(identData.getPipelineDetailIdent()));

    identDataList.forEach(identData -> {

      var padIdentData = pipelineIdentToDataSetMap.get(identData.getPipelineDetailIdent());

      padIdentData.forEach(padIdentDataItem -> {

        boolean mapped = false;

        try {
          verify(pipelineIdentDataMappingService, times(1)).mapPipelineIdentData(identData, padIdentDataItem);
          mapped = true;
        } catch (Exception e) {
          // try again
        }

        assertThat(mapped).isTrue();

      });

    });


  }

}