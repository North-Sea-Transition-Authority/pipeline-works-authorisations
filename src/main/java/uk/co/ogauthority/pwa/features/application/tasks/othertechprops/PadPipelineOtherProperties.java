package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;

@Entity
@Table(name = "pad_pipeline_other_properties")
public class PadPipelineOtherProperties implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @ManyToOne
  private PwaApplicationDetail pwaApplicationDetail;

  @Enumerated(EnumType.STRING)
  private OtherPipelineProperty propertyName;
  @Enumerated(EnumType.STRING)
  private PropertyAvailabilityOption availabilityOption;

  private BigDecimal minValue;
  private BigDecimal maxValue;


  public PadPipelineOtherProperties() {
    // default constructor required by hibernate
  }

  public PadPipelineOtherProperties(
      PwaApplicationDetail pwaApplicationDetail,
      OtherPipelineProperty propertyName) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.propertyName = propertyName;
  }

  //ChildEntity
  @Override
  public void clearId() {
    this.id = null;
  }

  @Override
  public void setParent(PwaApplicationDetail parentEntity) {
    this.pwaApplicationDetail = parentEntity;
  }

  @Override
  public PwaApplicationDetail getParent() {
    return this.pwaApplicationDetail;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public void setPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public OtherPipelineProperty getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(
      OtherPipelineProperty propertyName) {
    this.propertyName = propertyName;
  }

  public PropertyAvailabilityOption getAvailabilityOption() {
    return availabilityOption;
  }

  public void setAvailabilityOption(
      PropertyAvailabilityOption availabilityOption) {
    this.availabilityOption = availabilityOption;
  }

  public BigDecimal getMinValue() {
    return minValue;
  }

  public void setMinValue(BigDecimal minValue) {
    this.minValue = minValue;
  }

  public BigDecimal getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(BigDecimal maxValue) {
    this.maxValue = maxValue;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadPipelineOtherProperties that = (PadPipelineOtherProperties) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && propertyName == that.propertyName
        && availabilityOption == that.availabilityOption
        && Objects.equals(minValue, that.minValue)
        && Objects.equals(maxValue, that.maxValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, propertyName, availabilityOption, minValue, maxValue);
  }
}
