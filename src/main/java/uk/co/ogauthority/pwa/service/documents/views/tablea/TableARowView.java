package uk.co.ogauthority.pwa.service.documents.views.tablea;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentDiffableView;

public class TableARowView {

  private final String pipelineNumber;
  private final Integer identNumber;
  private final String fromLocation;
  private final CoordinatePair fromCoordinates;
  private final String toLocation;
  private final CoordinatePair toCoordinates;
  private final String componentParts;
  private final String length;
  private final String externalDiameter;
  private final String internalDiameter;
  private final String wallThickness;
  private final String typeOfInsulation;
  private final String maop;
  private final String productsToBeConveyed;



  public TableARowView(PipelineHeaderView pipelineHeaderView) {
    this.pipelineNumber = pipelineHeaderView.getPipelineNumber();
    this.identNumber = null;
    this.fromLocation = pipelineHeaderView.getFromLocation();
    this.fromCoordinates = pipelineHeaderView.getFromCoordinates();
    this.toLocation = pipelineHeaderView.getToLocation();
    this.toCoordinates = pipelineHeaderView.getToCoordinates();
    this.componentParts = pipelineHeaderView.getComponentParts();
    this.length = pipelineHeaderView.getLength().toString();
    this.externalDiameter = pipelineHeaderView.getMaxExternalDiameter() != null
        ? String.valueOf(pipelineHeaderView.getMaxExternalDiameter()) : null;
    this.internalDiameter = null;
    this.wallThickness = null;
    this.typeOfInsulation = null;
    this.maop = null;
    this.productsToBeConveyed = pipelineHeaderView.getProductsToBeConveyed();
  }

  public TableARowView(IdentDiffableView identDiffableView) {
    this.pipelineNumber = null;
    this.identNumber = identDiffableView.getIdentNumber();
    this.fromLocation = identDiffableView.getFromLocation();
    this.fromCoordinates = identDiffableView.getFromCoordinates();
    this.toLocation = identDiffableView.getToLocation();
    this.toCoordinates = identDiffableView.getToCoordinates();
    this.componentParts = identDiffableView.getComponentPartsDescription();
    this.length = identDiffableView.getLength();
    this.externalDiameter = identDiffableView.getExternalDiameter();
    this.internalDiameter = identDiffableView.getInternalDiameter();
    this.wallThickness = identDiffableView.getWallThickness();
    this.typeOfInsulation = identDiffableView.getInsulationCoatingType();
    this.maop = identDiffableView.getMaop();
    this.productsToBeConveyed = identDiffableView.getProductsToBeConveyed();
  }


  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public Integer getIdentNumber() {
    return identNumber;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public String getToLocation() {
    return toLocation;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public String getComponentParts() {
    return componentParts;
  }

  public String getLength() {
    return length;
  }

  public String getExternalDiameter() {
    return externalDiameter;
  }

  public String getInternalDiameter() {
    return internalDiameter;
  }

  public String getWallThickness() {
    return wallThickness;
  }

  public String getTypeOfInsulation() {
    return typeOfInsulation;
  }

  public String getMaop() {
    return maop;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TableARowView that = (TableARowView) o;
    return Objects.equals(pipelineNumber, that.pipelineNumber)
        && Objects.equals(identNumber, that.identNumber)
        && Objects.equals(fromLocation, that.fromLocation)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toLocation, that.toLocation)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(componentParts, that.componentParts)
        && Objects.equals(length, that.length)
        && Objects.equals(externalDiameter, that.externalDiameter)
        && Objects.equals(internalDiameter, that.internalDiameter)
        && Objects.equals(wallThickness, that.wallThickness)
        && Objects.equals(typeOfInsulation, that.typeOfInsulation)
        && Objects.equals(maop, that.maop)
        && Objects.equals(productsToBeConveyed, that.productsToBeConveyed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineNumber, identNumber, fromLocation, fromCoordinates, toLocation, toCoordinates,
        componentParts, length, externalDiameter, internalDiameter, wallThickness, typeOfInsulation, maop,
        productsToBeConveyed);
  }
}
