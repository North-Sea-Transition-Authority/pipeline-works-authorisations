package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;

/**
 * Provides pipeline overview and task list information for a given pipeline on an application.
 */
public class PadPipelineTaskListItem implements PipelineOverview {

  private final PipelineOverview pipelineOverview;
  private final List<TaskListEntry> tasks;
  private PadPipeline padPipeline;

  public PadPipelineTaskListItem(PipelineOverview pipelineOverview, List<TaskListEntry> tasks, PadPipeline padPipeline) {
    this.pipelineOverview = pipelineOverview;
    this.tasks = tasks;
    this.padPipeline = padPipeline;
  }

  public List<TaskListEntry> getTaskList() {
    return Collections.unmodifiableList(this.tasks);
  }

  @Override
  public Integer getPadPipelineId() {
    return this.pipelineOverview.getPadPipelineId();
  }

  public PadPipeline getPadPipeline() { return padPipeline;}

//  @Override
//  public String getPipelineName() {
//    return this.pipelineOverview.getPipelineName(padPipeline);
//  }

  @Override
  public String getFromLocation() {
    return this.pipelineOverview.getFromLocation();
  }

  @Override
  public CoordinatePair getFromCoordinates() {
    return this.pipelineOverview.getFromCoordinates();
  }

  @Override
  public String getToLocation() {
    return this.pipelineOverview.getToLocation();
  }

  @Override
  public CoordinatePair getToCoordinates() {
    return this.pipelineOverview.getToCoordinates();
  }

  @Override
  public String getPipelineNumber() {
    return this.pipelineOverview.getPipelineNumber();
  }

  @Override
  public PipelineType getPipelineType() {
    return this.pipelineOverview.getPipelineType();
  }

  @Override
  public String getComponentParts() {
    return this.pipelineOverview.getComponentParts();
  }

  @Override
  public BigDecimal getLength() {
    return this.pipelineOverview.getLength();
  }

  @Override
  public String getProductsToBeConveyed() {
    return this.pipelineOverview.getProductsToBeConveyed();
  }

  @Override
  public Long getNumberOfIdents() {
    return this.pipelineOverview.getNumberOfIdents();
  }
}
