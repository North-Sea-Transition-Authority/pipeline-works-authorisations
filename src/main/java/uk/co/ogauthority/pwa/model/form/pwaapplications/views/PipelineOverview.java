package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
// TODO This needs refactoring into the DTO overview part and the last list part
public class PipelineOverview {

  private Integer pipelineId;

  private String fromLocation;

  private CoordinatePair fromCoordinates;

  private String toLocation;

  private CoordinatePair toCoordinates;

  private String pipelineNumber;

  private PipelineType pipelineType;

  private String componentParts;

  private BigDecimal length;

  private String productsToBeConveyed;

  private List<TaskListEntry> tasks;

  public PipelineOverview(PadPipeline pipeline, List<TaskListEntry> tasks) {
    this.pipelineId = pipeline.getId();
    this.fromLocation = pipeline.getFromLocation();
    this.fromCoordinates = pipeline.getFromCoordinates();
    this.toLocation = pipeline.getToLocation();
    this.toCoordinates = pipeline.getToCoordinates();
    this.pipelineNumber = pipeline.getPipelineRef();
    this.pipelineType = pipeline.getPipelineType();
    this.componentParts = pipeline.getComponentPartsDescription();
    this.length = pipeline.getLength();
    this.productsToBeConveyed = pipeline.getProductsToBeConveyed();
    this.tasks = tasks;
  }

  public Integer getPipelineId() {
    return pipelineId;
  }

  public String getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(String fromLocation) {
    this.fromLocation = fromLocation;
  }

  public CoordinatePair getFromCoordinates() {
    return fromCoordinates;
  }

  public void setFromCoordinates(CoordinatePair fromCoordinates) {
    this.fromCoordinates = fromCoordinates;
  }

  public String getToLocation() {
    return toLocation;
  }

  public void setToLocation(String toLocation) {
    this.toLocation = toLocation;
  }

  public CoordinatePair getToCoordinates() {
    return toCoordinates;
  }

  public void setToCoordinates(CoordinatePair toCoordinates) {
    this.toCoordinates = toCoordinates;
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

  public String getComponentParts() {
    return componentParts;
  }

  public void setComponentParts(String componentParts) {
    this.componentParts = componentParts;
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

  public List<TaskListEntry> getTasks() {
    return tasks;
  }

  public void setTasks(List<TaskListEntry> tasks) {
    this.tasks = tasks;
  }

}
