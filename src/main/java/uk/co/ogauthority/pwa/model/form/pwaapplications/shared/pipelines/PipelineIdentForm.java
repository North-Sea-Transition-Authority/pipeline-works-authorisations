package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;

public class PipelineIdentForm {

  private String fromLocation;

  private CoordinateForm fromCoordinateForm;

  private String toLocation;

  private CoordinateForm toCoordinateForm;

  private BigDecimal length;
  private BigDecimal lengthOptional;
  private Boolean definingStructure;

  private PipelineIdentDataForm dataForm;

  public PipelineIdentForm() {
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  public CoordinateForm getFromCoordinateForm() {
    return fromCoordinateForm;
  }

  public void setFromCoordinateForm(CoordinateForm fromCoordinateForm) {
    this.fromCoordinateForm = fromCoordinateForm;
  }

  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public CoordinateForm getToCoordinateForm() {
    return toCoordinateForm;
  }

  public void setToCoordinateForm(CoordinateForm toCoordinateForm) {
    this.toCoordinateForm = toCoordinateForm;
  }

  public BigDecimal getLength() {
    return length;
  }

  public void setLength(BigDecimal length) {
    this.length = length;
  }

  public PipelineIdentDataForm getDataForm() {
    return dataForm;
  }

  public void setDataForm(PipelineIdentDataForm dataForm) {
    this.dataForm = dataForm;
  }

  public Boolean getDefiningStructure() {
    return definingStructure;
  }

  public void setDefiningStructure(Boolean definingStructure) {
    this.definingStructure = definingStructure;
  }

  public BigDecimal getLengthOptional() {
    return lengthOptional;
  }

  public void setLengthOptional(BigDecimal lengthOptional) {
    this.lengthOptional = lengthOptional;
  }
}
