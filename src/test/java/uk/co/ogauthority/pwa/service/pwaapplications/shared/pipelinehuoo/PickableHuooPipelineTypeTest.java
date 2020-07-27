package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSegment;

@RunWith(MockitoJUnitRunner.class)
public class PickableHuooPipelineTypeTest {
  private final String FROM_LOCATION = "Start location";
  private final String TO_LOCATION = "End location";

  private final String INVALID_LOCATION = StringUtils.repeat("X", 201);

  private final String UNKNOWN_FORMAT = "SomeString";
  private final String FULL_VALID = "FULL##ID:1";
  private final String SPLIT_INC_VALID = "SPLIT##ID:2##FROM_INC:Start location##TO_INC:End location";
  private final String SPLIT_EXC_VALID = "SPLIT##ID:2##FROM_EXC:Start location##TO_EXC:End location";

  private final String FULL_INVALID = "FULL##ID:abc";
  private final String SPLIT_INVALID = "SPLIT##ID:abc##FROM_INC:" + INVALID_LOCATION + "##TO_EXC:" + INVALID_LOCATION;

  private final int FULL_PIPELINE_ID = 1;
  private final int SPLIT_PIPELINE_ID = 2;


  @Before
  public void setup() {

  }

  @Test
  public void getTypeIdFromString_whenUnknownFormat() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(UNKNOWN_FORMAT)).isEqualTo(
        PickableHuooPipelineType.UNKNOWN);
  }

  @Test
  public void getTypeIdFromString_whenFullFormat_andValid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(FULL_VALID)).isEqualTo(PickableHuooPipelineType.FULL);
  }

  @Test
  public void getTypeIdFromString_whenFullFormat_andInvalid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(FULL_INVALID)).isEqualTo(PickableHuooPipelineType.UNKNOWN);
  }

  @Test
  public void getTypeIdFromString_whenSplitFormatInclusive_andValid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(SPLIT_INC_VALID)).isEqualTo(PickableHuooPipelineType.SPLIT);
  }

  @Test
  public void getTypeIdFromString_whenSplitFormatExclusive_andValid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(SPLIT_EXC_VALID)).isEqualTo(PickableHuooPipelineType.SPLIT);
  }

  @Test
  public void getTypeIdFromString_whenSplitFormat_andInvalid() {
    assertThat(PickableHuooPipelineType.getTypeIdFromString(SPLIT_INVALID)).isEqualTo(PickableHuooPipelineType.UNKNOWN);
  }

  @Test
  public void decodeString_whenFullFormat_andValid() {
    assertThat(PickableHuooPipelineType.decodeString(FULL_VALID)).contains(new PipelineId(FULL_PIPELINE_ID));
  }

  @Test
  public void decodeString_whenFullFormat_andInvalid() {
    assertThat(PickableHuooPipelineType.decodeString(FULL_INVALID)).isEmpty();
  }

  @Test
  public void decodeString_whenSplitFormatInclusive_andValid() {
    assertThat(PickableHuooPipelineType.decodeString(SPLIT_INC_VALID)).contains(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.inclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  public void decodeString_whenSplitFormatExclusive_andValid() {
    assertThat(PickableHuooPipelineType.decodeString(SPLIT_EXC_VALID)).contains(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
            PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  public void decodeString_whenSplitFormatFromIncToExc_andValid() {
    var fromIncToExc = "SPLIT##ID:2##FROM_INC:Start location##TO_EXC:End location";

    assertThat(PickableHuooPipelineType.decodeString(fromIncToExc)).contains(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  public void decodeString_whenSplitFormatFromExcToInc_andValid() {
    var fromIncToExc = "SPLIT##ID:2##FROM_EXC:Start location##TO_INC:End location";

    assertThat(PickableHuooPipelineType.decodeString(fromIncToExc)).contains(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
            PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.inclusivePoint(TO_LOCATION)
        ));
  }

  @Test
  public void decodeString_whenInvalidString() {
    assertThat(PickableHuooPipelineType.decodeString(UNKNOWN_FORMAT)).isEmpty();
  }

  @Test
  public void createPickableString_whenPipelineId(){
    assertThat(PickableHuooPipelineType.createPickableString(new PipelineId(FULL_PIPELINE_ID)))
    .isEqualTo("FULL##ID:" + FULL_PIPELINE_ID);
  }

  @Test
  public void createPickableString_whenPipelineSegment_withMixedPointInclusions(){
    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
        PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
        PipelineIdentPoint.inclusivePoint(TO_LOCATION)
    ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_EXC:" + FROM_LOCATION + "##TO_INC:" + TO_LOCATION);

    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_INC:" + FROM_LOCATION + "##TO_EXC:" + TO_LOCATION);
  }

  @Test
  public void createPickableString_whenPipelineSegment_withOnlyInclusivePoints(){
    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
            PipelineIdentPoint.inclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.inclusivePoint(TO_LOCATION)
        ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_INC:" + FROM_LOCATION + "##TO_INC:" + TO_LOCATION);

  }

  @Test
  public void createPickableString_whenPipelineSegment_withOnlyExclusivePoints(){
    assertThat(PickableHuooPipelineType.createPickableString(
        PipelineSegment.from(new PipelineId(SPLIT_PIPELINE_ID),
            PipelineIdentPoint.exclusivePoint(FROM_LOCATION),
            PipelineIdentPoint.exclusivePoint(TO_LOCATION)
        ))
    ).isEqualTo("SPLIT##ID:" + SPLIT_PIPELINE_ID + "##FROM_EXC:" + FROM_LOCATION + "##TO_EXC:" + TO_LOCATION);

  }






}