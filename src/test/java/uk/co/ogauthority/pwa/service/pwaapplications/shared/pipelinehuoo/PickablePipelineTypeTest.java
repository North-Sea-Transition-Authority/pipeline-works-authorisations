package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

@RunWith(MockitoJUnitRunner.class)
public class PickablePipelineTypeTest {
 private final String UNKNOWN = "SomeString";
  private final String CONSENTED = "1++CONSENTED";
  private final String APPLICATION = "2++APPLICATION";

  private final int PIPELINE_ID = 1;
  private final int PAD_PIPELINE_ID = 2;

  @Mock
  private PipelineDetail pipelineDetail;

  @Mock
  private PadPipelineSummaryDto padPipelineSummaryDto;

  @Before
  public void setup(){
    when(pipelineDetail.getPipelineId()).thenReturn(PIPELINE_ID);
    when(padPipelineSummaryDto.getPadPipelineId()).thenReturn(PAD_PIPELINE_ID);
  }

  @Test
  public void getTypeIdFromString_whenUnknownFormat() {
    assertThat(PickablePipelineType.getTypeIdFromString(UNKNOWN)).isEqualTo(PickablePipelineType.UNKNOWN);
  }

  @Test
  public void getTypeIdFromString_whenConsentedFormat() {
    assertThat(PickablePipelineType.getTypeIdFromString(CONSENTED)).isEqualTo(PickablePipelineType.CONSENTED);
  }

  @Test
  public void getTypeIdFromString_whenApplicationFormat() {
    assertThat(PickablePipelineType.getTypeIdFromString(APPLICATION)).isEqualTo(PickablePipelineType.APPLICATION);
  }

  @Test
  public void getIntegerIdFromString_whenUnknownFormat(){
    assertThat(PickablePipelineType.getIntegerIdFromString(UNKNOWN)).isNull();
  }

  @Test
  public void getIntegerIdFromString_whenConsentedFormat(){
    assertThat(PickablePipelineType.getIntegerIdFromString(CONSENTED)).isEqualTo(1);
  }

  @Test
  public void getIntegerIdFromString_whenUknownFormat(){
    assertThat(PickablePipelineType.getIntegerIdFromString(APPLICATION)).isEqualTo(2);
  }

  @Test
  public void  getPickableString_fromPipelineDetail(){
   assertThat(PickablePipelineType.getPickableString(pipelineDetail)).isEqualTo("1++CONSENTED");
  }

  @Test
  public void  getPickableString_fromPadPipelineDtoSummary(){
    assertThat(PickablePipelineType.getPickableString(padPipelineSummaryDto)).isEqualTo("2++APPLICATION");
  }
}