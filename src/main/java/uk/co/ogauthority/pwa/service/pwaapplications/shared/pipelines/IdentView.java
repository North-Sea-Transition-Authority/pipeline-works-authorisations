package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentData;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

/**
 * A view that combines both PadPipelineIdent and PadPipelineIdentData information.
 */
public class IdentView {

  private final Integer identId;
  private final PipelineCoreType pipelineCoreType;

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

  private final String externalDiameterMultiCore;
  private final String internalDiameterMultiCore;
  private final String wallThicknessMultiCore;
  private final String insulationCoatingTypeMultiCore;
  private final String maopMultiCore;
  private final String productsToBeConveyedMultiCore;
  private final Boolean definingStructure;


  public IdentView(PipelineIdentData identData) {
    var ident = identData.getPipelineIdent();
    this.identId = ident.getPipelineIdentId();
    this.pipelineCoreType = ident.getPipelineCoreType();

    this.fromCoordinates = ident.getFromCoordinates();
    this.toCoordinates = ident.getToCoordinates();
    this.fromLocation = ident.getFromLocation();
    this.toLocation = ident.getToLocation();
    this.identNumber = ident.getIdentNo();
    this.length = ident.getLength();
    this.definingStructure = ident.getIsDefiningStructure();
    this.componentPartsDescription = identData.getComponentPartsDesc();
    this.externalDiameter = identData.getExternalDiameter();
    this.insulationCoatingType = identData.getInsulationCoatingType();
    this.maop = identData.getMaop();
    this.productsToBeConveyed = identData.getProductsToBeConveyed();
    this.internalDiameter = identData.getInternalDiameter();
    this.wallThickness = identData.getWallThickness();

    this.externalDiameterMultiCore = identData.getExternalDiameterMultiCore();
    this.internalDiameterMultiCore = identData.getInternalDiameterMultiCore();
    this.wallThicknessMultiCore = identData.getWallThicknessMultiCore();
    this.maopMultiCore = identData.getMaopMultiCore();
    this.insulationCoatingTypeMultiCore = identData.getInsulationCoatingTypeMultiCore();
    this.productsToBeConveyedMultiCore = identData.getProductsToBeConveyedMultiCore();
  }


  public Integer getIdentId() {
    return identId;
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

  public Boolean getDefiningStructure() {
    return definingStructure;
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


  public String getExternalDiameterMultiCore() {
    return externalDiameterMultiCore;
  }

  public String getInternalDiameterMultiCore() {
    return internalDiameterMultiCore;
  }

  public String getWallThicknessMultiCore() {
    return wallThicknessMultiCore;
  }

  public String getInsulationCoatingTypeMultiCore() {
    return insulationCoatingTypeMultiCore;
  }

  public String getMaopMultiCore() {
    return maopMultiCore;
  }

  public String getProductsToBeConveyedMultiCore() {
    return productsToBeConveyedMultiCore;
  }

  public PipelineCoreType getPipelineCoreType() {
    return pipelineCoreType;
  }
}
