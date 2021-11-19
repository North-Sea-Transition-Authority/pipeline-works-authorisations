package uk.co.ogauthority.pwa.features.appprocessing.tasklist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;

@Service
public class PwaAppProcessingTaskListService {

  private final PwaAppProcessingTaskService pwaAppProcessingTaskService;

  @Autowired
  public PwaAppProcessingTaskListService(PwaAppProcessingTaskService pwaAppProcessingTaskService) {
    this.pwaAppProcessingTaskService = pwaAppProcessingTaskService;
  }

  public List<TaskListGroup> getTaskListGroups(PwaAppProcessingContext pwaAppProcessingContext) {

    var taskRequirementToTaskMap = PwaAppProcessingTask.stream()
        .filter(task -> pwaAppProcessingTaskService.canShowTask(task, pwaAppProcessingContext))
        .collect(Collectors.groupingBy(PwaAppProcessingTask::getTaskRequirement));

    var groupList = new ArrayList<TaskListGroup>();

    taskRequirementToTaskMap.forEach((requiredTask, tasks) -> {

      if (!tasks.isEmpty()) {

        var entryList = tasks.stream()
            .map(task -> pwaAppProcessingTaskService.getTaskListEntry(task, pwaAppProcessingContext))
            .collect(Collectors.toList());

        if (shouldLockAllTasks(pwaAppProcessingContext)) {
          entryList.stream()
              .filter(taskListEntry -> PwaAppProcessingTask
                  .resolveFromTaskName(taskListEntry.getTaskName()).getTaskAutoLockable() == TaskAutoLockable.YES)
              .forEach(entry -> entry.setTaskState(TaskState.LOCK));
        }

        groupList.add(new TaskListGroup(
            requiredTask.getDisplayName(),
            requiredTask.getDisplayOrder(),
            entryList));

      }

    });

    groupList.sort(Comparator.comparing(TaskListGroup::getDisplayOrder));

    return groupList;

  }

  /**
   * If we're an industry user or there's an open consent review, everything should be locked, return true.
   * Otherwise false.
   */
  private boolean shouldLockAllTasks(PwaAppProcessingContext processingContext) {
    return processingContext.getApplicationInvolvement().hasOnlyIndustryInvolvement()
        || processingContext.getApplicationInvolvement().getOpenConsentReview() == OpenConsentReview.YES;
  }

}
