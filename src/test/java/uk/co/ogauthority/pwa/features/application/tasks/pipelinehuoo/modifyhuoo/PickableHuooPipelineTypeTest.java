package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentPoint;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;

@ExtendWith(MockitoExtension.class)
class PickableHuooPipelineTypeTest {
  private final String FROM_LOCATION = "Start location";
  private final String TO_LOCATION = "End location";
  private final int POSITION = 1;

  private final String INVALID_LOCATION = StringUtils.repeat("X", 201);

  private final String UNKNOWN_FORMAT = "SomeString";
  private final String FULL_VALID = "FULL##ID:1";
  private final String SPLIT_INC_VALID = "SPLIT##ID:2##FROM_INC:Start location##TO_INC:End location##POSITION:1";
  private final String SPLIT_EXC_VALID = "SPLIT##ID:2##FROM_EXC:Start location##TO_EXC:End location##POSITION:1";

  private final String FULL_INVALID = "FULL##ID:abc";
  private final String SPLIT_INVALID = "SPLIT##ID:abc##FROM_INC:" + INVALID_LOCATION + "##TO_EXC:" + INVALID_LOCATION;

  private final int FULL_PIPELINE_ID = 1;
  private final int SPLIT_PIPELINE_ID = 2;


  @BeforeEach
  void setup() {

  }

  @Test
  void getTypeIdFromString_whenUnknownFormat() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(UNKNOWN_FORMAT)).isEqualTo(
        PickableHuooPipelineType.UNKNOWN);
  }

  @Test
  void getTypeIdFromString_whenFullFormat_andValid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(FULL_VALID)).isEqualTo(PickableHuooPipelineType.FULL);
  }

  @Test
  void getTypeIdFromString_whenFullFormat_andInvalid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(FULL_INVALID)).isEqualTo(PickableHuooPipelineType.UNKNOWN);
  }

  @Test
  void getTypeIdFromString_whenSplitFormatInclusive_andValid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(SPLIT_INC_VALID)).isEqualTo(PickableHuooPipelineType.SPLIT);
  }

  @Test
  void getTypeIdFromString_whenSplitFormatExclusive_andValid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(SPLIT_EXC_VALID)).isEqualTo(PickableHuooPipelineType.SPLIT);
  }

  @Test
  void getTypeIdFromString_whenSplitFormat_andInvalid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(SPLIT_INVALID)).isEqualTo(PickableHuooPipelineType.UNKNOWN);
  }

  @Test
  void decodeString_whenFullFormat_andValid() {
    assertThat(PickableHuooPipelineType.decodeString(FULL_VALID)).contains(new PipelineId(FULL_PIPELINE_ID));
  }

  @Test
  void decodeString_whenFullFormat_andInvalid() {
    assertThat(PickableHuooPipelineType.decodeString(FULL_INVALID)).isEmpty();
  }

  @Test
  void decodeString_whenSplitFormatInclusive_andValid() {
    assertThat(PickableHuooPipelineType.decodeString(SPLIT_INC_VALID)).contains(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            POSITION,
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.inclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  void decodeString_whenSplitFormatExclusive_andValid() {
    assertThat(PickableHuooPipelineType.decodeString(SPLIT_EXC_VALID)).contains(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            POSITION,
            PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  void decodeString_whenSplitFormatFromIncToExc_andValid() {
    var fromIncToExc = "SPLIT##ID:2##FROM_INC:Start location##TO_EXC:End location##POSITION:1";

    assertThat(PickableHuooPipelineType.decodeString(fromIncToExc)).contains(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            POSITION,
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  void decodeString_whenSplitFormatFromExcToInc_andValid() {
    var fromIncToExc = "SPLIT##ID:2##FROM_EXC:Start location##TO_INC:End location##POSITION:5";

    assertThat(PickableHuooPipelineType.decodeString(fromIncToExc)).contains(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            5,
            PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.inclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  void decodeString_whenInvalidString() {
    assertThat(PickableHuooPipelineType.decodeString(UNKNOWN_FORMAT)).isEmpty();
  }

  @Test
  void createPickableString_whenPipelineId(){
    assertThat(PickableHuooPipelineType.createPickableString(new PipelineId(FULL_PIPELINE_ID)))
    .isEqualTo("FULL##ID:" + FULL_PIPELINE_ID);
  }

  @Test
  void createPickableString_whenPipelineSection_withMixedPointInclusions(){
    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            POSITION,
            PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
        PipelineIdentPoint.inclusivePoint(TO_LOCATION)
    ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_EXC:" + FROM_LOCATION + "##TO_INC:" + TO_LOCATION + "##POSITION:" + POSITION);

    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            POSITION,
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_INC:" + FROM_LOCATION + "##TO_EXC:" + TO_LOCATION + "##POSITION:" + POSITION);
  }

  @Test
  void createPickableString_whenPipelineSection_withOnlyInclusivePoints(){
    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            POSITION,
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.inclusivePoint(TO_LOCATION)
        ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_INC:" + FROM_LOCATION + "##TO_INC:" + TO_LOCATION + "##POSITION:" + POSITION);

  }

  @Test
  void createPickableString_whenPipelineSection_withOnlyExclusivePoints(){
    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSection.from(new PipelineId(SPLIT_PIPELINE_ID),
            POSITION,
            PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_EXC:" + FROM_LOCATION + "##TO_EXC:" + TO_LOCATION + "##POSITION:" + POSITION);

  }






}