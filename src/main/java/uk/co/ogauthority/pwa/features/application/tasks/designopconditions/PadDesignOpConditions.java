package uk.co.ogauthority.pwa.features.application.tasks.designopconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.ChildEntity;


@Entity
@Table(name = "pad_design_op_conditions")
public class PadDesignOpConditions implements ChildEntity<Integer, PwaApplicationDetail> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "application_detail_id")
  @OneToOne
  private PwaApplicationDetail pwaApplicationDetail;

  @Column(name = "temperature_op_min")
  private BigDecimal temperatureOpMinValue;
  @Column(name = "temperature_op_max")
  private BigDecimal temperatureOpMaxValue;

  @Column(name = "temperature_design_min")
  private BigDecimal temperatureDesignMinValue;
  @Column(name = "temperature_design_max")
  private BigDecimal temperatureDesignMaxValue;

  @Column(name = "pressure_op_min")
  private BigDecimal pressureOpMinValue;
  @Column(name = "pressure_op_max")
  private BigDecimal pressureOpMaxValue;

  @Column(name = "pressure_design_max")
  private BigDecimal pressureDesignMaxValue;

  @Column(name = "flowrate_op_min")
  private BigDecimal flowrateOpMinValue;
  @Column(name = "flowrate_op_max")
  private BigDecimal flowrateOpMaxValue;

  @Column(name = "flowrate_design_min")
  private BigDecimal flowrateDesignMinValue;
  @Column(name = "flowrate_design_max")
  private BigDecimal flowrateDesignMaxValue;

  @Column(name = "flowrate_unit")
  @Enumerated(EnumType.STRING)
  private UnitMeasurement flowrateMeasurement;

  @Column(name = "co2_density_max")
  private BigDecimal co2DensityMaxValue;

  @Column(name = "co2_density_min")
  private BigDecimal co2DensityMinValue;

  private BigDecimal uvalueDesign;

  public PadDesignOpConditions(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PadDesignOpConditions() { // default constructor required by hibernate
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

  public BigDecimal getTemperatureOpMinValue() {
    return temperatureOpMinValue;
  }

  public void setTemperatureOpMinValue(BigDecimal temperatureOpMinValue) {
    this.temperatureOpMinValue = temperatureOpMinValue;
  }

  public BigDecimal getTemperatureOpMaxValue() {
    return temperatureOpMaxValue;
  }

  public void setTemperatureOpMaxValue(BigDecimal temperatureOpMaxValue) {
    this.temperatureOpMaxValue = temperatureOpMaxValue;
  }

  public BigDecimal getTemperatureDesignMinValue() {
    return temperatureDesignMinValue;
  }

  public void setTemperatureDesignMinValue(BigDecimal temperatureDesignMinValue) {
    this.temperatureDesignMinValue = temperatureDesignMinValue;
  }

  public BigDecimal getTemperatureDesignMaxValue() {
    return temperatureDesignMaxValue;
  }

  public void setTemperatureDesignMaxValue(BigDecimal temperatureDesignMaxValue) {
    this.temperatureDesignMaxValue = temperatureDesignMaxValue;
  }

  public BigDecimal getPressureOpMinValue() {
    return pressureOpMinValue;
  }

  public void setPressureOpMinValue(BigDecimal pressureOpMinValue) {
    this.pressureOpMinValue = pressureOpMinValue;
  }

  public BigDecimal getPressureOpMaxValue() {
    return pressureOpMaxValue;
  }

  public void setPressureOpMaxValue(BigDecimal pressureOpMaxValue) {
    this.pressureOpMaxValue = pressureOpMaxValue;
  }

  public BigDecimal getPressureDesignMaxValue() {
    return pressureDesignMaxValue;
  }

  public void setPressureDesignMaxValue(BigDecimal pressureDesignMaxValue) {
    this.pressureDesignMaxValue = pressureDesignMaxValue;
  }

  public BigDecimal getFlowrateOpMinValue() {
    return flowrateOpMinValue;
  }

  public void setFlowrateOpMinValue(BigDecimal flowrateOpMinValue) {
    this.flowrateOpMinValue = flowrateOpMinValue;
  }

  public BigDecimal getFlowrateOpMaxValue() {
    return flowrateOpMaxValue;
  }

  public void setFlowrateOpMaxValue(BigDecimal flowrateOpMaxValue) {
    this.flowrateOpMaxValue = flowrateOpMaxValue;
  }

  public BigDecimal getFlowrateDesignMinValue() {
    return flowrateDesignMinValue;
  }

  public void setFlowrateDesignMinValue(BigDecimal flowrateDesignMinValue) {
    this.flowrateDesignMinValue = flowrateDesignMinValue;
  }

  public BigDecimal getFlowrateDesignMaxValue() {
    return flowrateDesignMaxValue;
  }

  public void setFlowrateDesignMaxValue(BigDecimal flowrateDesignMaxValue) {
    this.flowrateDesignMaxValue = flowrateDesignMaxValue;
  }

  public UnitMeasurement getFlowrateMeasurement() {
    return flowrateMeasurement;
  }

  public PadDesignOpConditions setFlowrateMeasurement(
      UnitMeasurement flowrateMeasurement) {
    this.flowrateMeasurement = flowrateMeasurement;
    return this;
  }

  public BigDecimal getCo2DensityMaxValue() {
    return co2DensityMaxValue;
  }

  public PadDesignOpConditions setCo2DensityMaxValue(BigDecimal co2DensityMaxValue) {
    this.co2DensityMaxValue = co2DensityMaxValue;
    return this;
  }

  public BigDecimal getCo2DensityMinValue() {
    return co2DensityMinValue;
  }

  public PadDesignOpConditions setCo2DensityMinValue(BigDecimal co2DensityMinValue) {
    this.co2DensityMinValue = co2DensityMinValue;
    return this;
  }

  public BigDecimal getUvalueDesign() {
    return uvalueDesign;
  }

  public void setUvalueDesign(BigDecimal uvalueDesign) {
    this.uvalueDesign = uvalueDesign;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PadDesignOpConditions that = (PadDesignOpConditions) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetail, that.pwaApplicationDetail)
        && Objects.equals(temperatureOpMinValue, that.temperatureOpMinValue)
        && Objects.equals(temperatureOpMaxValue, that.temperatureOpMaxValue)
        && Objects.equals(temperatureDesignMinValue, that.temperatureDesignMinValue)
        && Objects.equals(temperatureDesignMaxValue, that.temperatureDesignMaxValue)
        && Objects.equals(pressureOpMinValue, that.pressureOpMinValue)
        && Objects.equals(pressureOpMaxValue, that.pressureOpMaxValue)
        && Objects.equals(pressureDesignMaxValue, that.pressureDesignMaxValue)
        && Objects.equals(flowrateOpMinValue, that.flowrateOpMinValue)
        && Objects.equals(flowrateOpMaxValue, that.flowrateOpMaxValue)
        && Objects.equals(flowrateDesignMinValue, that.flowrateDesignMinValue)
        && Objects.equals(flowrateDesignMaxValue, that.flowrateDesignMaxValue)
        && Objects.equals(flowrateMeasurement, that.flowrateMeasurement)
        && Objects.equals(co2DensityMaxValue, that.co2DensityMaxValue)
        && Objects.equals(co2DensityMinValue, that.co2DensityMinValue)
        && Objects.equals(uvalueDesign, that.uvalueDesign);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, temperatureOpMinValue, temperatureOpMaxValue,
        temperatureDesignMinValue,
        temperatureDesignMaxValue, pressureOpMinValue, pressureOpMaxValue,
        pressureDesignMaxValue, flowrateOpMinValue, flowrateOpMaxValue, flowrateDesignMinValue,
        flowrateDesignMaxValue, flowrateMeasurement, co2DensityMaxValue, co2DensityMinValue, uvalueDesign);
  }
}



