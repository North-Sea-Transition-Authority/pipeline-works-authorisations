package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@RunWith(MockitoJUnitRunner.class)
public class PipelineWriterTest {

  @Mock
  private PadPipelineIdentDataService padPipelineIdentDataService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  private PipelineWriter pipelineWriter;

  private PwaApplicationDetail detail;

  private Map<Pipeline, PadPipelineDto> pipelineToPadPipelineDtoMap;

  private ConsentWriterDto consentWriterDto;

  @Before
  public void setUp() throws Exception {

    detail = new PwaApplicationDetail();

    pipelineToPadPipelineDtoMap = PipelineWriterTestUtils.createPipelineToPadPipelineDtoMap();

    var identData = pipelineToPadPipelineDtoMap.values().stream()
        .flatMap(dto -> dto.getIdentToIdentDataSetMap().values().stream())
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    when(padPipelineIdentDataService.getAllPipelineIdentDataForPwaApplicationDetail(detail))
        .thenReturn(identData);

    pipelineWriter = new PipelineWriter(padPipelineIdentDataService, pipelineDetailService);

    consentWriterDto = new ConsentWriterDto();

  }

  @Test
  public void writerIsApplicable_hasPipelinesTask() {

    boolean isApplicable = pipelineWriter.writerIsApplicable(Set.of(ApplicationTask.PIPELINES), new PwaConsent());

    assertThat(isApplicable).isTrue();

  }

  @Test
  public void writerIsApplicable_doesNotHavePipelinesTask() {

    boolean isApplicable = pipelineWriter.writerIsApplicable(Set.of(ApplicationTask.HUOO), new PwaConsent());

    assertThat(isApplicable).isFalse();

  }

  @Test
  public void write() {

    var consent = new PwaConsent();
    pipelineWriter.write(detail, consent, consentWriterDto);

    verify(pipelineDetailService, times(1)).createNewPipelineDetails(pipelineToPadPipelineDtoMap, consent, consentWriterDto);

  }

}