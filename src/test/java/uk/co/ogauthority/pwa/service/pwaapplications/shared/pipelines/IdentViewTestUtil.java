package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;

public class IdentViewTestUtil {
  public static final String POINT_1 = "POINT1";
  public static final String POINT_2 = "POINT2";
  public static final String POINT_3 = "POINT3";
  public static final String POINT_4 = "POINT4";

  public static final int CURRENT_IDENT_NUMBER = 2;

  // put more SF than the formatter should use
  public static final double MOAP = 1.0501;
  public static final BigDecimal LENGTH = BigDecimal.valueOf(2.0001);
  public static final double INTERNAL = 2.0501;
  public static final double EXTERNAL = 3.0;

  public static final double WALL_THICKNESS = 4.0;

  public static final String MOAP_STRING = "1.05";
  public static final String LENGTH_STRING = "2";
  public static final String COMPONENT_PARTS_STRING = "Part 1, part2 , part 3";
  public static final String PRODUCTS_STRING_SINGLE = "Product 1, product 2 single";
  public static final String PRODUCTS_STRING_MULTI = "Product 1, product 2 multi";
  public static final String INTERNAL_STRING = "2.05";
  public static final String EXTERNAL_STRING = "3";
  public static final String INSULATION_STRING_SINGLE = "3.05 single";
  public static final String INSULATION_STRING_MULTI = "3.05 multi";
  public static final String WALL_THICKNESS_STRING = "4";

  public static final CoordinatePair FROM_COORD = CoordinatePairTestUtil.getDefaultCoordinate(45, 0);
  public static final CoordinatePair TO_COORD = CoordinatePairTestUtil.getDefaultCoordinate(46, 1);

  // should not be constructed
  private IdentViewTestUtil(){};

  public static void setupMultiCoreIdentViewMock(IdentView mockIdentView,
                                           String fromLocation,
                                           String toLocation,
                                           int identNumber) {
    setupIdentViewMock(mockIdentView, fromLocation, toLocation, identNumber, PipelineCoreType.MULTI_CORE);
    when(mockIdentView.getMaopMultiCore()).thenReturn(MOAP_STRING);
    when(mockIdentView.getLength()).thenReturn(LENGTH);
    when(mockIdentView.getExternalDiameterMultiCore()).thenReturn(EXTERNAL_STRING);
    when(mockIdentView.getInternalDiameterMultiCore()).thenReturn(INTERNAL_STRING);
    when(mockIdentView.getInsulationCoatingTypeMultiCore()).thenReturn(INSULATION_STRING_MULTI);
    when(mockIdentView.getWallThicknessMultiCore()).thenReturn(WALL_THICKNESS_STRING);
    when(mockIdentView.getProductsToBeConveyedMultiCore()).thenReturn(PRODUCTS_STRING_MULTI);
  }

  public static void setupSingleCoreIdentViewMock(IdentView mockIdentView,
                                            String fromLocation,
                                            String toLocation,
                                            int identNumber) {
    setupIdentViewMock(mockIdentView, fromLocation, toLocation, identNumber, PipelineCoreType.SINGLE_CORE);
    when(mockIdentView.getMaop()).thenReturn(BigDecimal.valueOf(MOAP));
    when(mockIdentView.getLength()).thenReturn(LENGTH);
    when(mockIdentView.getExternalDiameter()).thenReturn(BigDecimal.valueOf(EXTERNAL));
    when(mockIdentView.getInternalDiameter()).thenReturn(BigDecimal.valueOf(INTERNAL));
    when(mockIdentView.getInsulationCoatingType()).thenReturn(INSULATION_STRING_SINGLE);
    when(mockIdentView.getWallThickness()).thenReturn(BigDecimal.valueOf(WALL_THICKNESS));
    when(mockIdentView.getProductsToBeConveyed()).thenReturn(PRODUCTS_STRING_SINGLE);
  }

  public static void setupIdentViewMock(IdentView mockIdentView,
                                  String fromLocation,
                                  String toLocation,
                                  int identNumber,
                                  PipelineCoreType pipelineCoreType) {
    when(mockIdentView.getFromLocation()).thenReturn(fromLocation);
    when(mockIdentView.getToLocation()).thenReturn(toLocation);
    when(mockIdentView.getPipelineCoreType()).thenReturn(pipelineCoreType);
    when(mockIdentView.getIdentNumber()).thenReturn(identNumber);
    when(mockIdentView.getComponentPartsDescription()).thenReturn(COMPONENT_PARTS_STRING);
    when(mockIdentView.getFromCoordinates()).thenReturn(FROM_COORD);
    when(mockIdentView.getToCoordinates()).thenReturn(TO_COORD);


  }


}
