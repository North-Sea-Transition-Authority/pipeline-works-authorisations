package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;

public class PipelineDetailIdConverterTest {

  private static final int PIPELINE_DETAIL_ID = 1;
  private static final PipelineDetailId PIPELINE_DETAIL_ID_OBJ = new PipelineDetailId(PIPELINE_DETAIL_ID);
  private PipelineDetailIdConverter pipelineDetailIdConverter;

  @Before
  public void setup(){
    pipelineDetailIdConverter = new PipelineDetailIdConverter();
  }

  @Test
  public void convertToDatabaseColumn_whenNotNull() {

    assertThat(pipelineDetailIdConverter.convertToDatabaseColumn(PIPELINE_DETAIL_ID_OBJ)).isEqualTo(PIPELINE_DETAIL_ID);
  }

  @Test
  public void convertToDatabaseColumn_whenNull() {

    assertThat(pipelineDetailIdConverter.convertToDatabaseColumn(null)).isNull();
  }

  @Test
  public void convertToEntityAttribute_whenNotNull() {
    assertThat(pipelineDetailIdConverter.convertToEntityAttribute(PIPELINE_DETAIL_ID)).isEqualTo(PIPELINE_DETAIL_ID_OBJ);
  }

  @Test
  public void convertToEntityAttribute_whenNull() {
    assertThat(pipelineDetailIdConverter.convertToEntityAttribute(null)).isNull();
  }
}