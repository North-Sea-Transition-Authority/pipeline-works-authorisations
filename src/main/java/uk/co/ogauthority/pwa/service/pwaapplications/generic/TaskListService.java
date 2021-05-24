package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskListGroup;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTaskGroup;

/**
 * Service which is responsible for delivering the task list as a whole for a given application detail.
 */
@Service
public class TaskListService {

  private final TaskListEntryFactory taskListEntryFactory;
  private final ApplicationTaskService applicationTaskService;

  @Autowired
  public TaskListService(TaskListEntryFactory taskListEntryFactory,
                         ApplicationTaskService applicationTaskService) {
    this.applicationTaskService = applicationTaskService;
    this.taskListEntryFactory = taskListEntryFactory;
  }

  /**
   * <p>For a given application return true if one or more task in the provided set is shown in the task list.</p>
   */
  public boolean anyTaskShownForApplication(Set<ApplicationTask> applicationTaskSet,
                                            PwaApplicationDetail pwaApplicationDetail) {
    // This step required because we only have access to the whole list designed for use in the templates.
    return applicationTaskSet.stream()
        .anyMatch(applicationTask -> applicationTaskService.canShowTask(applicationTask, pwaApplicationDetail));

  }

  /**
   * <p>This is visible for use in integration tests only. Use {@see TaskListService::getTaskListGroups()} for screen representation
   * or {@see TaskListService::getShownApplicationTasksForDetail()} for the list of tasks shown in an application.</p>
   */
  @VisibleForTesting
  public List<TaskListEntry> getApplicationTaskListEntries(PwaApplicationDetail detail) {

    List<TaskListEntry> tasks = getShownApplicationTasksForDetail(detail).stream()
        .map(task -> taskListEntryFactory.createApplicationTaskListEntry(detail, task))
        .sorted(Comparator.comparing(TaskListEntry::getDisplayOrder))
        .collect(Collectors.toList());

    if (tasks.isEmpty()) {
      tasks.add(taskListEntryFactory.createNoTasksEntry(detail.getPwaApplication()));
    }

    return tasks;
  }

  public List<TaskListGroup> getTaskListGroups(PwaApplicationDetail pwaApplicationDetail) {
    Set<ApplicationTask> shownApplicationTasks = new HashSet<>(getShownApplicationTasksForDetail(pwaApplicationDetail));
    return ApplicationTaskGroup.asList()
        .stream()
        // filter out groups where no tasks in the group are shown
        .filter(applicationTaskGroup -> !SetUtils.intersection(
            applicationTaskGroup.getApplicationTaskSet(),
            shownApplicationTasks
            ).isEmpty()
        )
        // for each group where tasks are shown, create a task list group object
        .map(applicationTaskGroup -> {
          // per group, filter out tasks that are not shown.
          var visibleTasksInGroup = applicationTaskGroup.getTasks().stream()
              .filter(o -> shownApplicationTasks.contains(o.getApplicationTask()))
              .collect(Collectors.toList());

          return new TaskListGroup(
              applicationTaskGroup.getDisplayName(),
              applicationTaskGroup.getDisplayOrder(),
              visibleTasksInGroup.stream()
                  .map(orderedTaskGroupTask -> taskListEntryFactory.createApplicationTaskListEntry(pwaApplicationDetail,
                      orderedTaskGroupTask))
                  // sort the tasks by their display order
                  .sorted(Comparator.comparing(TaskListEntry::getDisplayOrder))
                  .collect(Collectors.toList())
          );
        })
        // sort the groups by their display order
        .sorted(Comparator.comparing(TaskListGroup::getDisplayOrder))
        .collect(Collectors.toList());
  }

  /**
   * Check the application type AND specific information about the detail passed in to determine which tasks are relevant.
   */
  public List<ApplicationTask> getShownApplicationTasksForDetail(PwaApplicationDetail detail) {

    return ApplicationTask.stream()
        .filter(task -> applicationTaskService.canShowTask(task, detail))
        .collect(Collectors.toList());

  }

  /**
   * Return tasks that by default should be shown for the app type.
   */
  public List<ApplicationTask> getApplicationTasksForAppType(PwaApplicationType pwaApplicationType) {

    return ApplicationTask.stream()
        .filter(task -> applicationTaskService.appTypeSupportsTask(pwaApplicationType, task))
        .collect(Collectors.toList());

  }

}
