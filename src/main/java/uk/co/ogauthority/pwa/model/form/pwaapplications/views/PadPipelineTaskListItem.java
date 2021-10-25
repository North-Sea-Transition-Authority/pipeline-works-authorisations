package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist.PadPipelineTaskListHeader;

/**
 * Provides pipeline info and task list information for a given pipeline on an application.
 */
public class PadPipelineTaskListItem {

  private final int padPipelineId;
  private final long numberOfIdents;
  private final PipelineStatus pipelineStatus;
  private final String pipelineName;
  private final boolean hasTasks;

  private final List<TaskListEntry> tasks;

  public PadPipelineTaskListItem(PadPipelineTaskListHeader padPipelineTaskListHeader,
                                 List<TaskListEntry> tasks) {
    this.padPipelineId = padPipelineTaskListHeader.getPadPipelineId();
    this.numberOfIdents = padPipelineTaskListHeader.getNumberOfIdents();
    this.pipelineStatus = padPipelineTaskListHeader.getPipelineStatus();
    this.pipelineName = padPipelineTaskListHeader.getPipelineName();
    this.hasTasks = !tasks.isEmpty();
    this.tasks = tasks;
  }

  public int getPadPipelineId() {
    return padPipelineId;
  }

  public long getNumberOfIdents() {
    return numberOfIdents;
  }

  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public List<TaskListEntry> getTaskList() {
    return Collections.unmodifiableList(this.tasks);
  }

  public Boolean getHasTasks() {
    return hasTasks;
  }
}
