package uk.co.ogauthority.pwa.model.entity.pipelines;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;

@Entity
@Table(name = "pipeline_details")
public class PipelineDetail {

  @Id
  private Integer id;
  private String pipelineId;
  private Instant startTimestamp;
  private Instant endTimestamp;
  private String tipFlag;
  private String pipelineStatus;
  private String detailStatus;
  private String pipelineReference;

  @Enumerated(EnumType.STRING)
  private PipelineType pipelineType;
  private String fromLocation;
  private String fromLatDeg;
  private String fromLatMin;
  private String fromLatSec;
  private String fromLatDir;
  private String fromLongDeg;
  private String fromLongMin;
  private String fromLongSec;
  private String fromLongDir;
  private String toLocation;
  private String toLatDeg;
  private String toLatMin;
  private String toLatSec;
  private String toLatDir;
  private String toLongDeg;
  private String toLongMin;
  private String toLongSec;
  private String toLongDir;
  private String componentPartsDesc;
  private String length;
  private String productsToBeConveyed;
  private String trenchedBuriedFilledFlag;
  private String trenchingMethodsDesc;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public String getPipelineId() {
    return pipelineId;
  }

  public void setPipelineId(String pipelineId) {
    this.pipelineId = pipelineId;
  }


  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Instant startTimestamp) {
    this.startTimestamp = startTimestamp;
  }


  public Instant getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Instant endTimestamp) {
    this.endTimestamp = endTimestamp;
  }


  public String getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(String tipFlag) {
    this.tipFlag = tipFlag;
  }


  public String getPipelineStatus() {
    return pipelineStatus;
  }

  public void setPipelineStatus(String pipelineStatus) {
    this.pipelineStatus = pipelineStatus;
  }


  public String getDetailStatus() {
    return detailStatus;
  }

  public void setDetailStatus(String detailStatus) {
    this.detailStatus = detailStatus;
  }


  public String getPipelineReference() {
    return pipelineReference;
  }

  public void setPipelineReference(String pipelineReference) {
    this.pipelineReference = pipelineReference;
  }


  public PipelineType getPipelineType() {
    return pipelineType;
  }

  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }


  public String getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }


  public String getFromLatDeg() {
    return fromLatDeg;
  }

  public void setFromLatDeg(String fromLatDeg) {
    this.fromLatDeg = fromLatDeg;
  }


  public String getFromLatMin() {
    return fromLatMin;
  }

  public void setFromLatMin(String fromLatMin) {
    this.fromLatMin = fromLatMin;
  }


  public String getFromLatSec() {
    return fromLatSec;
  }

  public void setFromLatSec(String fromLatSec) {
    this.fromLatSec = fromLatSec;
  }


  public String getFromLatDir() {
    return fromLatDir;
  }

  public void setFromLatDir(String fromLatDir) {
    this.fromLatDir = fromLatDir;
  }


  public String getFromLongDeg() {
    return fromLongDeg;
  }

  public void setFromLongDeg(String fromLongDeg) {
    this.fromLongDeg = fromLongDeg;
  }


  public String getFromLongMin() {
    return fromLongMin;
  }

  public void setFromLongMin(String fromLongMin) {
    this.fromLongMin = fromLongMin;
  }


  public String getFromLongSec() {
    return fromLongSec;
  }

  public void setFromLongSec(String fromLongSec) {
    this.fromLongSec = fromLongSec;
  }


  public String getFromLongDir() {
    return fromLongDir;
  }

  public void setFromLongDir(String fromLongDir) {
    this.fromLongDir = fromLongDir;
  }


  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }


  public String getToLatDeg() {
    return toLatDeg;
  }

  public void setToLatDeg(String toLatDeg) {
    this.toLatDeg = toLatDeg;
  }


  public String getToLatMin() {
    return toLatMin;
  }

  public void setToLatMin(String toLatMin) {
    this.toLatMin = toLatMin;
  }


  public String getToLatSec() {
    return toLatSec;
  }

  public void setToLatSec(String toLatSec) {
    this.toLatSec = toLatSec;
  }


  public String getToLatDir() {
    return toLatDir;
  }

  public void setToLatDir(String toLatDir) {
    this.toLatDir = toLatDir;
  }


  public String getToLongDeg() {
    return toLongDeg;
  }

  public void setToLongDeg(String toLongDeg) {
    this.toLongDeg = toLongDeg;
  }


  public String getToLongMin() {
    return toLongMin;
  }

  public void setToLongMin(String toLongMin) {
    this.toLongMin = toLongMin;
  }


  public String getToLongSec() {
    return toLongSec;
  }

  public void setToLongSec(String toLongSec) {
    this.toLongSec = toLongSec;
  }


  public String getToLongDir() {
    return toLongDir;
  }

  public void setToLongDir(String toLongDir) {
    this.toLongDir = toLongDir;
  }


  public String getComponentPartsDesc() {
    return componentPartsDesc;
  }

  public void setComponentPartsDesc(String componentPartsDesc) {
    this.componentPartsDesc = componentPartsDesc;
  }


  public String getLength() {
    return length;
  }

  public void setLength(String length) {
    this.length = length;
  }


  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }


  public String getTrenchedBuriedFilledFlag() {
    return trenchedBuriedFilledFlag;
  }

  public void setTrenchedBuriedFilledFlag(String trenchedBuriedFilledFlag) {
    this.trenchedBuriedFilledFlag = trenchedBuriedFilledFlag;
  }


  public String getTrenchingMethodsDesc() {
    return trenchingMethodsDesc;
  }

  public void setTrenchingMethodsDesc(String trenchingMethodsDesc) {
    this.trenchingMethodsDesc = trenchingMethodsDesc;
  }

}
