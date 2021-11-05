package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.util.StringDisplayUtils;

/**
 * A view that combines both ident and identData information in a simplified format. Easy to consume using DiffService.
 */
public class IdentDiffableView {

  private final Boolean connectedToNext;
  private final Boolean connectedToPrevious;

  private final CoordinatePair fromCoordinates;
  private final CoordinatePair toCoordinates;

  private final String fromLocation;
  private final String toLocation;

  private final Integer identNumber;
  private final String length;

  private final String componentPartsDescription;

  private final String externalDiameter;
  private final String insulationCoatingType;
  private final String maop;
  private final String productsToBeConveyed;
  private final String internalDiameter;
  private final String wallThickness;
  private final Boolean definingStructure;

  private IdentDiffableView(Boolean connectedToNext,
                            Boolean connectedToPrevious,
                            CoordinatePair fromCoordinates,
                            CoordinatePair toCoordinates,
                            String fromLocation,
                            String toLocation,
                            Integer identNumber,
                            String length,
                            String componentPartsDescription,
                            String externalDiameter,
                            String insulationCoatingType,
                            String maop,
                            String productsToBeConveyed,
                            String internalDiameter,
                            String wallThickness,
                            Boolean definingStructure) {
    this.connectedToNext = connectedToNext;
    this.connectedToPrevious = connectedToPrevious;
    this.fromCoordinates = fromCoordinates;
    this.toCoordinates = toCoordinates;
    this.fromLocation = fromLocation;
    this.toLocation = toLocation;
    this.identNumber = identNumber;
    this.length = length;
    this.componentPartsDescription = componentPartsDescription;
    this.externalDiameter = externalDiameter;
    this.insulationCoatingType = insulationCoatingType;
    this.maop = maop;
    this.productsToBeConveyed = productsToBeConveyed;
    this.internalDiameter = internalDiameter;
    this.wallThickness = wallThickness;
    this.definingStructure = definingStructure;
  }


  public static IdentDiffableView fromIdentViews(IdentView previousIdentView,
                                                 IdentView identView,
                                                 IdentView nextIdentView) {

    var connectedToPrevious = Optional.ofNullable(previousIdentView)
        .map(piv -> StringUtils.defaultIfEmpty(piv.getToLocation(), "").equals(identView.getFromLocation()))
        .orElse(false);

    var connectedToNext = Optional.ofNullable(nextIdentView)
        .map(niv -> StringUtils.defaultIfEmpty(niv.getFromLocation(), "").equals(identView.getToLocation()))
        .orElse(false);

    return new IdentDiffableView(
        connectedToNext,
        connectedToPrevious,
        identView.getFromCoordinates(),
        identView.getToCoordinates(),
        identView.getFromLocation(),
        identView.getToLocation(),
        identView.getIdentNumber(),
        StringDisplayUtils.formatDecimal2DpOrNull(identView.getLength()),
        identView.getComponentPartsDescription(),
        // External diameter
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getExternalDiameterMultiCore()
            : StringDisplayUtils.formatDecimal2DpOrNull(identView.getExternalDiameter()),
        // Insulation coating type
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getInsulationCoatingTypeMultiCore()
            : identView.getInsulationCoatingType(),
        // MOAP
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getMaopMultiCore()
            : StringDisplayUtils.formatDecimal2DpOrNull(identView.getMaop()),
        // Products conveyed
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getProductsToBeConveyedMultiCore()
            : identView.getProductsToBeConveyed(),
        // Internal diameter
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getInternalDiameterMultiCore()
            : StringDisplayUtils.formatDecimal2DpOrNull(identView.getInternalDiameter()),
        // Wall thickness
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getWallThicknessMultiCore()
            : StringDisplayUtils.formatDecimal2DpOrNull(identView.getWallThickness()),
        identView.getDefiningStructure());

  }
  
  public Boolean getConnectedToNext() {
    return connectedToNext;
  }

  public Boolean getConnectedToPrevious() {
    return connectedToPrevious;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public String getToLocation() {
    return toLocation;
  }

  public Integer getIdentNumber() {
    return identNumber;
  }

  public String getLength() {
    return length;
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public String getExternalDiameter() {
    return externalDiameter;
  }

  public String getInsulationCoatingType() {
    return insulationCoatingType;
  }

  public String getMaop() {
    return maop;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public String getInternalDiameter() {
    return internalDiameter;
  }

  public String getWallThickness() {
    return wallThickness;
  }

  public Boolean getDefiningStructure() {
    return definingStructure;
  }
}
