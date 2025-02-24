package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;

class PipelineDetailIdConverterTest {

  private static final int PIPELINE_DETAIL_ID = 1;
  private static final PipelineDetailId PIPELINE_DETAIL_ID_OBJ = new PipelineDetailId(PIPELINE_DETAIL_ID);
  private PipelineDetailIdConverter pipelineDetailIdConverter;

  @BeforeEach
  void setup(){
    pipelineDetailIdConverter = new PipelineDetailIdConverter();
  }

  @Test
  void convertToDatabaseColumn_whenNotNull() {

    assertThat(pipelineDetailIdConverter.convertToDatabaseColumn(PIPELINE_DETAIL_ID_OBJ)).isEqualTo(PIPELINE_DETAIL_ID);
  }

  @Test
  void convertToDatabaseColumn_whenNull() {

    assertThat(pipelineDetailIdConverter.convertToDatabaseColumn(null)).isNull();
  }

  @Test
  void convertToEntityAttribute_whenNotNull() {
    assertThat(pipelineDetailIdConverter.convertToEntityAttribute(PIPELINE_DETAIL_ID)).isEqualTo(PIPELINE_DETAIL_ID_OBJ);
  }

  @Test
  void convertToEntityAttribute_whenNull() {
    assertThat(pipelineDetailIdConverter.convertToEntityAttribute(null)).isNull();
  }
}