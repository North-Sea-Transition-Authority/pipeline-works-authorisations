package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/**
 * A view that combines both ident and identData information in a simplified format. Easy to give consume using DiffService.
 */
public class IdentDiffableView {

  private static final DecimalFormat DECIMAL_FORMAT_2DP = new DecimalFormat("#.##");

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
                           String wallThickness) {
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
        formatDecimalOrNull(identView.getLength()),
        identView.getComponentPartsDescription(),
        // External diameter
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getExternalDiameterMultiCore()
            : formatDecimalOrNull(identView.getExternalDiameter()),
        // Insulation coating type
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getInsulationCoatingTypeMultiCore()
            : identView.getInsulationCoatingType(),
        // MOAP
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getMaopMultiCore()
            : formatDecimalOrNull(identView.getMaop()),
        // Products conveyed
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getProductsToBeConveyedMultiCore()
            : identView.getProductsToBeConveyed(),
        // Internal diameter
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getInternalDiameterMultiCore()
            : formatDecimalOrNull(identView.getInternalDiameter()),
        // Wall thickness
        identView.getPipelineCoreType().equals(PipelineCoreType.MULTI_CORE)
            ? identView.getWallThicknessMultiCore()
            : formatDecimalOrNull(identView.getWallThickness())
    );

  }

  public static DecimalFormat getDecimalFormat2dp() {
    return DECIMAL_FORMAT_2DP;
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

  private static String formatDecimalOrNull(BigDecimal bigDecimal) {
    return bigDecimal != null ? DECIMAL_FORMAT_2DP.format(bigDecimal) : null;
  }
}
