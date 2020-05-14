package uk.co.ogauthority.pwa.model.entity.pipelines;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pipeline_detail_idents")
public class PipelineDetailIdent {

  @Id
  private Integer id;
  private String pipelineDetailId;
  private String identNo;
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
  private String length;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }


  public String getPipelineDetailId() {
    return pipelineDetailId;
  }

  public void setPipelineDetailId(String pipelineDetailId) {
    this.pipelineDetailId = pipelineDetailId;
  }


  public String getIdentNo() {
    return identNo;
  }

  public void setIdentNo(String identNo) {
    this.identNo = identNo;
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


  public String getLength() {
    return length;
  }

  public void setLength(String length) {
    this.length = length;
  }

}
