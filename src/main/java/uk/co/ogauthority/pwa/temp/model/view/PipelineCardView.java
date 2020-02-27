package uk.co.ogauthority.pwa.temp.model.view;

import java.util.List;

public class PipelineCardView {

  private PipelineView pipelineView;
  private String pipelineOverviewUrl;
  private List<TaskListEntry> taskListEntryList;

  public PipelineCardView(PipelineView pipelineView,
                          String pipelineOverviewUrl, List<TaskListEntry> taskListEntryList) {
    this.pipelineView = pipelineView;
    this.pipelineOverviewUrl = pipelineOverviewUrl;
    this.taskListEntryList = taskListEntryList;
  }

  public PipelineView getPipelineView() {
    return pipelineView;
  }

  public List<TaskListEntry> getTaskListEntryList() {
    return taskListEntryList;
  }

  public String getPipelineOverviewUrl() {
    return pipelineOverviewUrl;
  }
}
