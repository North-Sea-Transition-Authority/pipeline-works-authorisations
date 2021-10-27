package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

@RunWith(MockitoJUnitRunner.class)
public class IdentDiffableViewTest {

  private final String POINT_1 = IdentViewTestUtil.POINT_1;
  private final String POINT_2 = IdentViewTestUtil.POINT_2;
  private final String POINT_3 = IdentViewTestUtil.POINT_3;
  private final String POINT_4 = IdentViewTestUtil.POINT_4;

  private final int CURRENT_IDENT_NUMBER = IdentViewTestUtil.CURRENT_IDENT_NUMBER;

  // put more SF than the formatter should use
  private final double MOAP = IdentViewTestUtil.MOAP;
  private final BigDecimal LENGTH = IdentViewTestUtil.LENGTH;
  private final double INTERNAL = IdentViewTestUtil.INTERNAL;
  private final double EXTERNAL = IdentViewTestUtil.EXTERNAL;

  private final double WALL_THICKNESS = IdentViewTestUtil.WALL_THICKNESS;

  private final String MOAP_STRING = IdentViewTestUtil.MOAP_STRING;
  private final String LENGTH_STRING = IdentViewTestUtil.LENGTH_STRING;
  private final String COMPONENT_PARTS_STRING = IdentViewTestUtil.COMPONENT_PARTS_STRING;
  private final String PRODUCTS_STRING_SINGLE = IdentViewTestUtil.PRODUCTS_STRING_SINGLE;
  private final String PRODUCTS_STRING_MULTI = IdentViewTestUtil.PRODUCTS_STRING_MULTI;
  private final String INTERNAL_STRING = IdentViewTestUtil.INTERNAL_STRING;
  private final String EXTERNAL_STRING = IdentViewTestUtil.EXTERNAL_STRING;
  private final String INSULATION_STRING_SINGLE = IdentViewTestUtil.INSULATION_STRING_SINGLE;
  private final String INSULATION_STRING_MULTI = IdentViewTestUtil.INSULATION_STRING_MULTI;
  private final String WALL_THICKNESS_STRING = IdentViewTestUtil.WALL_THICKNESS_STRING;

  private final CoordinatePair FROM_COORD = IdentViewTestUtil.FROM_COORD;
  private final CoordinatePair TO_COORD = IdentViewTestUtil.TO_COORD;

  @Mock
  private IdentView previousIdent;

  @Mock
  private IdentView currentIdent;

  @Mock
  private IdentView nextIdent;


  @Before
  public void setUp() throws Exception {

    IdentViewTestUtil.setupSingleCoreIdentViewMock(currentIdent, POINT_2, POINT_3, CURRENT_IDENT_NUMBER);
  }

  @Test
  public void fromIdentViews_whenNoPreviousIdent_andNoNextIdent() {

    var result = IdentDiffableView.fromIdentViews(null, currentIdent, null);

    assertThat(result.getConnectedToNext()).isFalse();
    assertThat(result.getConnectedToPrevious()).isFalse();

  }

  @Test
  public void fromIdentViews_whenNoPreviousIdent_andNextIdentNotConnected() {

    IdentViewTestUtil.setupSingleCoreIdentViewMock(nextIdent, POINT_4, POINT_4, CURRENT_IDENT_NUMBER + 1);

    var result = IdentDiffableView.fromIdentViews(null, currentIdent, nextIdent);

    assertThat(result.getConnectedToNext()).isFalse();
    assertThat(result.getConnectedToPrevious()).isFalse();

  }

  @Test
  public void fromIdentViews_whenNoPreviousIdent_andNextIdentIsConnected() {

    IdentViewTestUtil.setupSingleCoreIdentViewMock(nextIdent, POINT_3, POINT_4, CURRENT_IDENT_NUMBER + 1);

    var result = IdentDiffableView.fromIdentViews(null, currentIdent, nextIdent);

    assertThat(result.getConnectedToNext()).isTrue();
    assertThat(result.getConnectedToPrevious()).isFalse();

  }

  @Test
  public void fromIdentViews_whenPreviousIdent_andNoNextIdent() {

    IdentViewTestUtil.setupSingleCoreIdentViewMock(previousIdent, POINT_1, POINT_2, CURRENT_IDENT_NUMBER - 1);

    var result = IdentDiffableView.fromIdentViews(previousIdent, currentIdent, null);

    assertThat(result.getConnectedToNext()).isFalse();
    assertThat(result.getConnectedToPrevious()).isTrue();

  }


  @Test
  public void fromIdentViews_whenSingleCoreIdent() {


    var result = IdentDiffableView.fromIdentViews(null, currentIdent, null);

    assertThat(result.getFromLocation()).isEqualTo(POINT_2);
    assertThat(result.getToLocation()).isEqualTo(POINT_3);
    assertThat(result.getIdentNumber()).isEqualTo(CURRENT_IDENT_NUMBER);
    assertThat(result.getLength()).isEqualTo(LENGTH_STRING);
    assertThat(result.getComponentPartsDescription()).isEqualTo(COMPONENT_PARTS_STRING);
    assertThat(result.getExternalDiameter()).isEqualTo(EXTERNAL_STRING);
    assertThat(result.getInsulationCoatingType()).isEqualTo(INSULATION_STRING_SINGLE);
    assertThat(result.getMaop()).isEqualTo(MOAP_STRING);
    assertThat(result.getProductsToBeConveyed()).isEqualTo(PRODUCTS_STRING_SINGLE);
    assertThat(result.getInternalDiameter()).isEqualTo(INTERNAL_STRING);
    assertThat(result.getWallThickness()).isEqualTo(WALL_THICKNESS_STRING);
    assertThat(result.getFromCoordinates()).isEqualTo(FROM_COORD);
    assertThat(result.getToCoordinates()).isEqualTo(TO_COORD);

  }

  @Test
  public void fromIdentViews_whenMultiCoreIdent() {

    IdentViewTestUtil.setupMultiCoreIdentViewMock(currentIdent, POINT_1, POINT_2, CURRENT_IDENT_NUMBER);
    var result = IdentDiffableView.fromIdentViews(null, currentIdent, null);

    assertThat(result.getFromLocation()).isEqualTo(POINT_1);
    assertThat(result.getToLocation()).isEqualTo(POINT_2);
    assertThat(result.getIdentNumber()).isEqualTo(CURRENT_IDENT_NUMBER);
    assertThat(result.getLength()).isEqualTo(LENGTH_STRING);
    assertThat(result.getComponentPartsDescription()).isEqualTo(COMPONENT_PARTS_STRING);
    assertThat(result.getExternalDiameter()).isEqualTo(EXTERNAL_STRING);
    assertThat(result.getInsulationCoatingType()).isEqualTo(INSULATION_STRING_MULTI);
    assertThat(result.getMaop()).isEqualTo(MOAP_STRING);
    assertThat(result.getProductsToBeConveyed()).isEqualTo(PRODUCTS_STRING_MULTI);
    assertThat(result.getInternalDiameter()).isEqualTo(INTERNAL_STRING);
    assertThat(result.getWallThickness()).isEqualTo(WALL_THICKNESS_STRING);
    assertThat(result.getFromCoordinates()).isEqualTo(FROM_COORD);
    assertThat(result.getToCoordinates()).isEqualTo(TO_COORD);

  }



}