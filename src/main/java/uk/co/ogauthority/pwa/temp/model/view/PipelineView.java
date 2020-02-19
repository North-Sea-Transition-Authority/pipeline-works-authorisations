package uk.co.ogauthority.pwa.temp.model.view;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.temp.model.service.PipelineType;

public class PipelineView implements Serializable {

  private String pipelineNumber;

  private PipelineType pipelineType;

  private String from;

  private String to;

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

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
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
