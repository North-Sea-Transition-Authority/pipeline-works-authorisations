package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


@Entity
@Table(name = "pad_design_op_conditions")
public class PadDesignOpConditions {

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

  @Column(name = "pressure_op_intl")
  private BigDecimal pressureOpInternalValue;
  @Column(name = "pressure_op_extl")
  private BigDecimal pressureOpExternalValue;

  @Column(name = "pressure_design_intl")
  private BigDecimal pressureDesignInternalValue;
  @Column(name = "pressure_design_extl")
  private BigDecimal pressureDesignExternalValue;

  @Column(name = "flowrate_op_min")
  private BigDecimal flowrateOpMinValue;
  @Column(name = "flowrate_op_max")
  private BigDecimal flowrateOpMaxValue;

  @Column(name = "flowrate_design_min")
  private BigDecimal flowrateDesignMinValue;
  @Column(name = "flowrate_design_max")
  private BigDecimal flowrateDesignMaxValue;

  private BigDecimal uvalueOp;
  private BigDecimal uvalueDesign;

  public PadDesignOpConditions(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PadDesignOpConditions() { // default constructor required by hibernate
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

  public BigDecimal getPressureOpInternalValue() {
    return pressureOpInternalValue;
  }

  public void setPressureOpInternalValue(BigDecimal pressureOpInternalValue) {
    this.pressureOpInternalValue = pressureOpInternalValue;
  }

  public BigDecimal getPressureOpExternalValue() {
    return pressureOpExternalValue;
  }

  public void setPressureOpExternalValue(BigDecimal pressureOpExternalValue) {
    this.pressureOpExternalValue = pressureOpExternalValue;
  }

  public BigDecimal getPressureDesignInternalValue() {
    return pressureDesignInternalValue;
  }

  public void setPressureDesignInternalValue(BigDecimal pressureDesignInternalValue) {
    this.pressureDesignInternalValue = pressureDesignInternalValue;
  }

  public BigDecimal getPressureDesignExternalValue() {
    return pressureDesignExternalValue;
  }

  public void setPressureDesignExternalValue(BigDecimal pressureDesignExternalValue) {
    this.pressureDesignExternalValue = pressureDesignExternalValue;
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

  public BigDecimal getUvalueOp() {
    return uvalueOp;
  }

  public void setUvalueOp(BigDecimal uvalueOp) {
    this.uvalueOp = uvalueOp;
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
        && Objects.equals(pressureOpInternalValue, that.pressureOpInternalValue)
        && Objects.equals(pressureOpExternalValue, that.pressureOpExternalValue)
        && Objects.equals(pressureDesignInternalValue, that.pressureDesignInternalValue)
        && Objects.equals(pressureDesignExternalValue, that.pressureDesignExternalValue)
        && Objects.equals(flowrateOpMinValue, that.flowrateOpMinValue)
        && Objects.equals(flowrateOpMaxValue, that.flowrateOpMaxValue)
        && Objects.equals(flowrateDesignMinValue, that.flowrateDesignMinValue)
        && Objects.equals(flowrateDesignMaxValue, that.flowrateDesignMaxValue)
        && Objects.equals(uvalueOp, that.uvalueOp)
        && Objects.equals(uvalueDesign, that.uvalueDesign);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplicationDetail, temperatureOpMinValue, temperatureOpMaxValue,
        temperatureDesignMinValue,
        temperatureDesignMaxValue, pressureOpInternalValue, pressureOpExternalValue, pressureDesignInternalValue,
        pressureDesignExternalValue, flowrateOpMinValue, flowrateOpMaxValue, flowrateDesignMinValue,
        flowrateDesignMaxValue, uvalueOp, uvalueDesign);
  }
}



