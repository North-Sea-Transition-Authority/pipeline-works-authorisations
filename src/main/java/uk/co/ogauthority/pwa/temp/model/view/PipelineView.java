package uk.co.ogauthority.pwa.temp.model.view;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;

public class PipelineView implements Serializable {

  private String pipelineNumber;

  private PipelineType pipelineType;

  private String from;

  private String fromLatitudeDegrees;
  private String fromLatitudeMinutes;
  private String fromLatitudeSeconds;

  private String fromLongitudeDegrees;
  private String fromLongitudeMinutes;
  private String fromLongitudeSeconds;

  private String to;

  private String toLatitudeDegrees;
  private String toLatitudeMinutes;
  private String toLatitudeSeconds;

  private String toLongitudeDegrees;
  private String toLongitudeMinutes;
  private String toLongitudeSeconds;

  private String componentParts;

  private Integer length;

  private String productsToBeConveyed;

  private List<SubPipelineView> subPipelines;

  public PipelineView() {
  }

  public PipelineView(String pipelineNumber,
                      PipelineType pipelineType,
                      List<SubPipelineView> subPipelines) {
    this.pipelineNumber = pipelineNumber;
    this.pipelineType = pipelineType;
    this.subPipelines = subPipelines;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public void setPipelineNumber(String pipelineNumber) {
    this.pipelineNumber = pipelineNumber;
  }

  public PipelineType getPipelineType() {
    return pipelineType;
  }

  public void setPipelineType(PipelineType pipelineType) {
    this.pipelineType = pipelineType;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getFromLatitudeDegrees() {
    return fromLatitudeDegrees;
  }

  public void setFromLatitudeDegrees(String fromLatitudeDegrees) {
    this.fromLatitudeDegrees = fromLatitudeDegrees;
  }

  public String getFromLatitudeMinutes() {
    return fromLatitudeMinutes;
  }

  public void setFromLatitudeMinutes(String fromLatitudeMinutes) {
    this.fromLatitudeMinutes = fromLatitudeMinutes;
  }

  public String getFromLatitudeSeconds() {
    return fromLatitudeSeconds;
  }

  public void setFromLatitudeSeconds(String fromLatitudeSeconds) {
    this.fromLatitudeSeconds = fromLatitudeSeconds;
  }

  public String getFromLongitudeDegrees() {
    return fromLongitudeDegrees;
  }

  public void setFromLongitudeDegrees(String fromLongitudeDegrees) {
    this.fromLongitudeDegrees = fromLongitudeDegrees;
  }

  public String getFromLongitudeMinutes() {
    return fromLongitudeMinutes;
  }

  public void setFromLongitudeMinutes(String fromLongitudeMinutes) {
    this.fromLongitudeMinutes = fromLongitudeMinutes;
  }

  public String getFromLongitudeSeconds() {
    return fromLongitudeSeconds;
  }

  public void setFromLongitudeSeconds(String fromLongitudeSeconds) {
    this.fromLongitudeSeconds = fromLongitudeSeconds;
  }

  public String getFromLatString() {
    return "<br/> " + this.fromLatitudeDegrees + "&deg; " + this.fromLatitudeMinutes + "' " + this.fromLatitudeSeconds + "\" N";
  }

  public String getFromLongString() {
    return "<br/> " + this.fromLongitudeDegrees + "&deg; " + this.fromLongitudeMinutes + "' " + this.fromLongitudeSeconds + "\" E";
  }

  public String getToLatString() {
    return "<br/> " + this.toLatitudeDegrees + "&deg; " + this.toLatitudeMinutes + "' " + this.toLatitudeSeconds + "\" N";
  }

  public String getToLongString() {
    return "<br/> " + this.toLongitudeDegrees + "&deg; " + this.toLongitudeMinutes + "' " + this.toLongitudeSeconds + "\" E";

  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getToLatitudeDegrees() {
    return toLatitudeDegrees;
  }

  public void setToLatitudeDegrees(String toLatitudeDegrees) {
    this.toLatitudeDegrees = toLatitudeDegrees;
  }

  public String getToLatitudeMinutes() {
    return toLatitudeMinutes;
  }

  public void setToLatitudeMinutes(String toLatitudeMinutes) {
    this.toLatitudeMinutes = toLatitudeMinutes;
  }

  public String getToLatitudeSeconds() {
    return toLatitudeSeconds;
  }

  public void setToLatitudeSeconds(String toLatitudeSeconds) {
    this.toLatitudeSeconds = toLatitudeSeconds;
  }

  public String getToLongitudeDegrees() {
    return toLongitudeDegrees;
  }

  public void setToLongitudeDegrees(String toLongitudeDegrees) {
    this.toLongitudeDegrees = toLongitudeDegrees;
  }

  public String getToLongitudeMinutes() {
    return toLongitudeMinutes;
  }

  public void setToLongitudeMinutes(String toLongitudeMinutes) {
    this.toLongitudeMinutes = toLongitudeMinutes;
  }

  public String getToLongitudeSeconds() {
    return toLongitudeSeconds;
  }

  public void setToLongitudeSeconds(String toLongitudeSeconds) {
    this.toLongitudeSeconds = toLongitudeSeconds;
  }

  public String getComponentParts() {
    return componentParts;
  }

  public void setComponentParts(String componentParts) {
    this.componentParts = componentParts;
  }

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public String getProductsToBeConveyed() {
    return productsToBeConveyed;
  }

  public void setProductsToBeConveyed(String productsToBeConveyed) {
    this.productsToBeConveyed = productsToBeConveyed;
  }

  public List<SubPipelineView> getSubPipelines() {
    return subPipelines;
  }

  public void setSubPipelines(List<SubPipelineView> subPipelines) {
    this.subPipelines = subPipelines;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineView that = (PipelineView) o;
    return Objects.equals(pipelineNumber, that.pipelineNumber)
        && pipelineType == that.pipelineType
        && Objects.equals(from, that.from)
        && Objects.equals(to, that.to)
        && Objects.equals(componentParts, that.componentParts)
        && Objects.equals(length, that.length)
        && Objects.equals(productsToBeConveyed, that.productsToBeConveyed)
        && Objects.equals(subPipelines, that.subPipelines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineNumber, pipelineType, from, to, componentParts, length, productsToBeConveyed,
        subPipelines);
  }
}
