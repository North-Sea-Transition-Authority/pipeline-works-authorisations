package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;

/**
 * Provides pipeline overview details and task entreis for a given pipeline
 */
public final class PipelineTaskListItem {

  private final PipelineOverview pipelineOverview;
  private final List<TaskListEntry> tasks;

  public PipelineTaskListItem(PipelineOverview pipelineOverview, List<TaskListEntry> tasks) {
    this.pipelineOverview = pipelineOverview;
    this.tasks = tasks;
  }

  public Iterator<TaskListEntry> getTaskList() {
    return tasks.listIterator();
  }

  public Integer getPipelineId() {
    return this.pipelineOverview.getPipelineId();
  }

  public String getFromLocation() {
    return this.pipelineOverview.getFromLocation();
  }

  public CoordinatePair getFromCoordinates() {
    return this.pipelineOverview.getFromCoordinates();
  }

  public String getToLocation() {
    return this.pipelineOverview.getToLocation();
  }

  public CoordinatePair getToCoordinates() {
    return this.pipelineOverview.getToCoordinates();
  }

  public String getPipelineNumber() {
    return this.pipelineOverview.getPipelineNumber();
  }

  public PipelineType getPipelineType() {
    return this.pipelineOverview.getPipelineType();
  }

  public String getComponentParts() {
    return this.pipelineOverview.getComponentParts();
  }

  public BigDecimal getLength() {
    return this.pipelineOverview.getLength();
  }
}
