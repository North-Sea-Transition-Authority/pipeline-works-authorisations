package uk.co.ogauthority.pwa.service.documents.views;

import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;

public class DepositTableRowView {

  private final String depositReference;
  private final String pipelineNumbers;
  private final String proposedDate;
  private final String typeAndSizeOfMaterials;
  private final String quantity;
  private final CoordinatePair fromCoordinates;
  private final CoordinatePair toCoordinates;
  private final List<String> drawingNumbers;

  public DepositTableRowView(String depositReference,
                             String pipelineNumbers,
                             String proposedDate,
                             String typeAndSizeOfMaterials,
                             String quantity,
                             CoordinatePair fromCoordinates,
                             CoordinatePair toCoordinates,
                             List<String> drawingNumbers) {
    this.depositReference = depositReference;
    this.pipelineNumbers = pipelineNumbers;
    this.proposedDate = proposedDate;
    this.typeAndSizeOfMaterials = typeAndSizeOfMaterials;
    this.quantity = quantity;
    this.fromCoordinates = fromCoordinates;
    this.toCoordinates = toCoordinates;
    this.drawingNumbers = drawingNumbers;
  }



  public String getDepositReference() {
    return depositReference;
  }

  public String getPipelineNumbers() {
    return pipelineNumbers;
  }

  public String getProposedDate() {
    return proposedDate;
  }

  public String getTypeAndSizeOfMaterials() {
    return typeAndSizeOfMaterials;
  }

  public String getQuantity() {
    return quantity;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public List<String> getDrawingNumbers() {
    return drawingNumbers;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DepositTableRowView that = (DepositTableRowView) o;
    return Objects.equals(depositReference, that.depositReference)
        && Objects.equals(pipelineNumbers, that.pipelineNumbers)
        && Objects.equals(proposedDate, that.proposedDate)
        && Objects.equals(typeAndSizeOfMaterials, that.typeAndSizeOfMaterials)
        && Objects.equals(quantity, that.quantity)
        && Objects.equals(fromCoordinates, that.fromCoordinates)
        && Objects.equals(toCoordinates, that.toCoordinates)
        && Objects.equals(drawingNumbers, that.drawingNumbers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(depositReference, pipelineNumbers, proposedDate, typeAndSizeOfMaterials, quantity, fromCoordinates,
        toCoordinates, drawingNumbers);
  }
}
