package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/**
 * A view that combines both PadPipelineIdent and PadPipelineIdentData information.
 */
public class IdentView {

  private final CoordinatePair fromCoordinates;
  private final CoordinatePair toCoordinates;

  private final String fromLocation;
  private final String toLocation;

  private final Integer identNumber;
  private final BigDecimal length;

  private final String componentPartsDescription;
  private final BigDecimal externalDiameter;
  private final String insulationCoatingType;
  private final BigDecimal maop;
  private final String productsToBeConveyed;
  private final BigDecimal internalDiameter;
  private final BigDecimal wallThickness;

  public IdentView(PadPipelineIdentData identData) {
    var ident = identData.getPadPipelineIdent();
    this.fromCoordinates = ident.getFromCoordinates();
    this.toCoordinates = ident.getToCoordinates();
    this.fromLocation = ident.getFromLocation();
    this.toLocation = ident.getToLocation();
    this.identNumber = ident.getIdentNo();
    this.length = ident.getLength();
    this.componentPartsDescription = identData.getComponentPartsDescription();
    this.externalDiameter = identData.getExternalDiameter();
    this.insulationCoatingType = identData.getInsulationCoatingType();
    this.maop = identData.getMaop();
    this.productsToBeConveyed = identData.getProductsToBeConveyed();
    this.internalDiameter = identData.getInternalDiameter();
    this.wallThickness = identData.getWallThickness();
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

  public BigDecimal getLength() {
    return length;
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public BigDecimal getExternalDiameter() {
    return externalDiameter;
  }

  public String getInsulationCoatingType() {
    return insulationCoatingType;
  }

  public BigDecimal getMaop() {
    return maop;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public BigDecimal getInternalDiameter() {
    return internalDiameter;
  }

  public BigDecimal getWallThickness() {
    return wallThickness;
  }
}
