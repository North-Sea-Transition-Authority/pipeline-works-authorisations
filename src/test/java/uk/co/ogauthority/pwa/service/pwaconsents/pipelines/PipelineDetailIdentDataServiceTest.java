package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.PipelineWriterTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDetailIdentDataServiceTest {

  @Mock
  private PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;

  @Mock
  private PipelineIdentDataMappingService pipelineIdentDataMappingService;

  @Captor
  private ArgumentCaptor<List<PipelineDetailIdentData>> identDataCaptor;

  private PipelineDetailIdentDataService pipelineDetailIdentDataService;

  private Map<PipelineDetailIdent, Set<PadPipelineIdentData>> pipelineIdentToDataSetMap;

  @Before
  public void setUp() throws Exception {

    pipelineIdentToDataSetMap = PipelineWriterTestUtils.createIdentToDataSetMap();

    pipelineDetailIdentDataService = new PipelineDetailIdentDataService(pipelineDetailIdentDataRepository, pipelineIdentDataMappingService);

  }

  @Test
  public void createPipelineDetailIdentData() {

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