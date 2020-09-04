package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

/**
 * Provides all information about a specific application task which appears in the core task list.
 */
@Service
public class ApplicationTaskService {

  private final ApplicationContext applicationContext;

  @Autowired
  public ApplicationTaskService(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Helper which asks Spring to provide the app task service if its available.
   */
  private ApplicationFormSectionService getTaskService(GeneralPurposeApplicationTask applicationTask) {
    if (applicationTask.getServiceClass() == null) {
      throw new IllegalStateException(String.format("Application task doesn't have service class specified: %s",
          applicationTask.toString()));
    }

    return applicationContext.getBean(applicationTask.getServiceClass());
  }

  /**
   * A task can be shown for an application detail if the app type meets task criteria and the
   * app specific checks are met.
   */
  public boolean canShowTask(GeneralPurposeApplicationTask applicationTask, PwaApplicationDetail pwaApplicationDetail) {
    var taskService = getTaskService(applicationTask);
    var taskAppTypes = getValidApplicationTypesForTask(applicationTask);

    // Is the task valid for app type and does the specific app detail qualify for task?
    return taskAppTypes.contains(pwaApplicationDetail.getPwaApplicationType())
        && taskService.canShowInTaskList(pwaApplicationDetail);
  }

  /**
   * Return a list of additional information about an applications task.
   */
  List<TaskInfo> getTaskInfoList(GeneralPurposeApplicationTask applicationTask,
                                 PwaApplicationDetail pwaApplicationDetail) {
    return getTaskService(applicationTask).getTaskInfoList(pwaApplicationDetail);
  }

  /**
   * For a given Task, duplicate all task data for "fromDetail" to the "toDetail".
   */
  @Transactional
  public void copyApplicationTaskDataToApplicationDetail(ApplicationTask applicationTask,
                                              PwaApplicationDetail fromDetail,
                                              PwaApplicationDetail toDetail) {
    getTaskService(applicationTask).copySectionInformation(fromDetail, toDetail);

  }

  /**
   * Return true when all questions answered under a task are valid for an application detail.
   */
  boolean isTaskComplete(GeneralPurposeApplicationTask applicationTask, PwaApplicationDetail pwaApplicationDetail) {
    return getTaskService(applicationTask).isComplete(pwaApplicationDetail);
  }


  private Set<PwaApplicationType> getValidApplicationTypesForTask(GeneralPurposeApplicationTask applicationTask) {
    return Optional.ofNullable(applicationTask.getControllerClass())
        // this allows us to test method logic by returning an arbitrary class from the applicationContext in tests
        .map(controllerClass -> applicationContext.getBean(controllerClass).getClass())
        .map(clazz -> clazz.getAnnotation(PwaApplicationTypeCheck.class))
        .map(typeCheck -> Set.of(typeCheck.types()))
        // task has valid app type if controller has no type restriction
        .orElse(EnumSet.allOf(PwaApplicationType.class));
  }

}
