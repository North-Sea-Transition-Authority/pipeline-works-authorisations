package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import java.util.Optional;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

public class PipelineHeaderForm {

  private String fromLocation;
  private Integer fromLatDeg;
  private Integer fromLatMin;
  private BigDecimal fromLatSec;
  private LatitudeDirection fromLatDirection;

  private Integer fromLongDeg;
  private Integer fromLongMin;
  private BigDecimal fromLongSec;
  private LongitudeDirection fromLongDirection;

  private String toLocation;
  private Integer toLatDeg;
  private Integer toLatMin;
  private BigDecimal toLatSec;
  private LatitudeDirection toLatDirection;

  private Integer toLongDeg;
  private Integer toLongMin;
  private BigDecimal toLongSec;
  private LongitudeDirection toLongDirection;

  private PipelineType pipelineType;

  private String componentPartsDescription;

  private BigDecimal length;

  private String productsToBeConveyed;

  private Boolean trenchedBuriedBackfilled;
  private String trenchingMethods;

  public PipelineHeaderForm() {
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  public Integer getFromLatDeg() {
    return fromLatDeg;
  }

  public void setFromLatDeg(Integer fromLatDeg) {
    this.fromLatDeg = fromLatDeg;
  }

  public Integer getFromLatMin() {
    return fromLatMin;
  }

  public void setFromLatMin(Integer fromLatMin) {
    this.fromLatMin = fromLatMin;
  }

  public BigDecimal getFromLatSec() {
    return fromLatSec;
  }

  public void setFromLatSec(BigDecimal fromLatSec) {
    this.fromLatSec = fromLatSec;
  }

  public LatitudeDirection getFromLatDirection() {
    return Optional.ofNullable(fromLatDirection).orElse(LatitudeDirection.NORTH);
  }

  public void setFromLatDirection(LatitudeDirection fromLatDirection) {
    this.fromLatDirection = fromLatDirection;
  }

  public Integer getFromLongDeg() {
    return fromLongDeg;
  }

  public void setFromLongDeg(Integer fromLongDeg) {
    this.fromLongDeg = fromLongDeg;
  }

  public Integer getFromLongMin() {
    return fromLongMin;
  }

  public void setFromLongMin(Integer fromLongMin) {
    this.fromLongMin = fromLongMin;
  }

  public BigDecimal getFromLongSec() {
    return fromLongSec;
  }

  public void setFromLongSec(BigDecimal fromLongSec) {
    this.fromLongSec = fromLongSec;
  }

  public LongitudeDirection getFromLongDirection() {
    return fromLongDirection;
  }

  public void setFromLongDirection(LongitudeDirection fromLongDirection) {
    this.fromLongDirection = fromLongDirection;
  }

  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public Integer getToLatDeg() {
    return toLatDeg;
  }

  public void setToLatDeg(Integer toLatDeg) {
    this.toLatDeg = toLatDeg;
  }

  public Integer getToLatMin() {
    return toLatMin;
  }

  public void setToLatMin(Integer toLatMin) {
    this.toLatMin = toLatMin;
  }

  public BigDecimal getToLatSec() {
    return toLatSec;
  }

  public void setToLatSec(BigDecimal toLatSec) {
    this.toLatSec = toLatSec;
  }

  public LatitudeDirection getToLatDirection() {
    return Optional.ofNullable(toLatDirection).orElse(LatitudeDirection.NORTH);
  }

  public void setToLatDirection(LatitudeDirection toLatDirection) {
    this.toLatDirection = toLatDirection;
  }

  public Integer getToLongDeg() {
    return toLongDeg;
  }

  public void setToLongDeg(Integer toLongDeg) {
    this.toLongDeg = toLongDeg;
  }

  public Integer getToLongMin() {
    return toLongMin;
  }

  public void setToLongMin(Integer toLongMin) {
    this.toLongMin = toLongMin;
  }

  public BigDecimal getToLongSec() {
    return toLongSec;
  }

  public void setToLongSec(BigDecimal toLongSec) {
    this.toLongSec = toLongSec;
  }

  public LongitudeDirection getToLongDirection() {
    return toLongDirection;
  }

  public void setToLongDirection(LongitudeDirection toLongDirection) {
    this.toLongDirection = toLongDirection;
  }

  public PipelineType getPipelineType() {
    return pipelineType;
  }

  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }

  public String getComponentPartsDescription() {
    return componentPartsDescription;
  }

  public void setComponentPartsDescription(String componentPartsDescription) {
    this.componentPartsDescription = componentPartsDescription;
  }

  public BigDecimal getLength() {
    return length;
  }

  public void setLength(BigDecimal length) {
    this.length = length;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

  public Boolean getTrenchedBuriedBackfilled() {
    return trenchedBuriedBackfilled;
  }

  public void setTrenchedBuriedBackfilled(Boolean trenchedBuriedBackfilled) {
    this.trenchedBuriedBackfilled = trenchedBuriedBackfilled;
  }

  public String getTrenchingMethods() {
    return trenchingMethods;
  }

  public void setTrenchingMethods(String trenchingMethods) {
    this.trenchingMethods = trenchingMethods;
  }
}
