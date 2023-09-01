package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import java.util.Collections;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;

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
  private final boolean withdrawnTransfer;

  public PadPipelineTaskListItem(PadPipelineTaskListHeader padPipelineTaskListHeader,
                                 List<TaskListEntry> tasks) {
    this.padPipelineId = padPipelineTaskListHeader.getPadPipelineId();
    this.numberOfIdents = padPipelineTaskListHeader.getNumberOfIdents();
    this.pipelineStatus = padPipelineTaskListHeader.getPipelineStatus();
    this.pipelineName = padPipelineTaskListHeader.getPipelineName();
    this.hasTasks = !tasks.isEmpty();
    this.tasks = tasks;
    this.withdrawnTransfer = padPipelineTaskListHeader.isWithdrawnTransfer();
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

  public boolean isWithdrawnTransfer() {
    return withdrawnTransfer;
  }
}
