package uk.co.ogauthority.pwa.service.appprocessing.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;

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

        groupList.add(new TaskListGroup(
            requiredTask.getDisplayName(),
            requiredTask.getDisplayOrder(),
            entryList));

      }

    });

    groupList.sort(Comparator.comparing(TaskListGroup::getDisplayOrder));

    return groupList;

  }

}
