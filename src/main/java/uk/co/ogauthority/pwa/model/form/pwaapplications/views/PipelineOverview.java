package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;

public class PipelineOverview {

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

  public PipelineOverview() {
  }

  public PipelineOverview(String fromLocation, CoordinatePair fromCoordinates, String toLocation,
                          CoordinatePair toCoordinates, String pipelineNumber,
                          PipelineType pipelineType, String componentParts, BigDecimal length,
                          String productsToBeConveyed,
                          List<TaskListEntry> tasks) {
    this.fromLocation = fromLocation;
    this.fromCoordinates = fromCoordinates;
    this.toLocation = toLocation;
    this.toCoordinates = toCoordinates;
    this.pipelineNumber = pipelineNumber;
    this.pipelineType = pipelineType;
    this.componentParts = componentParts;
    this.length = length;
    this.productsToBeConveyed = productsToBeConveyed;
    this.tasks = tasks;
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
