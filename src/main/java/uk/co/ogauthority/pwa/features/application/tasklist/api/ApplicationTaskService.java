package uk.co.ogauthority.pwa.features.application.tasklist.api;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaResourceTypeCheck;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

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

    // Is the task valid for app type and does the specific app detail qualify for task?
    return appTypeSupportsTask(pwaApplicationDetail.getPwaApplicationType(), applicationTask)
        && resourceTypeSupportsTask(pwaApplicationDetail.getResourceType(), applicationTask)
        && taskService.canShowInTaskList(pwaApplicationDetail);
  }

  public boolean appTypeSupportsTask(PwaApplicationType pwaApplicationType, GeneralPurposeApplicationTask applicationTask) {

    var taskAppTypes = getValidApplicationTypesForTask(applicationTask);

    return taskAppTypes.contains(pwaApplicationType);

  }

  public boolean resourceTypeSupportsTask(PwaResourceType pwaResourceType, GeneralPurposeApplicationTask applicationTask) {

    var taskResourceTypes = getValidResourceTypesForTask(applicationTask);

    return taskResourceTypes.contains(pwaResourceType);

  }

  /**
   * Return a list of additional information about an applications task.
   */
  List<TaskInfo> getTaskInfoList(GeneralPurposeApplicationTask applicationTask,
                                 PwaApplicationDetail pwaApplicationDetail) {
    return getTaskService(applicationTask).getTaskInfoList(pwaApplicationDetail);
  }

  /**
   * For a given Task, ask the associated service if copy of the section information is appropriate.
   * Restricts based on controller type restrictions and then asks the service for consistency with canShowTask
   * functionality.
   */
  @Transactional
  public boolean taskAllowsCopySectionInformation(ApplicationTask applicationTask,
                                                  PwaApplicationDetail pwaApplicationDetail) {
    var taskAppTypes = getValidApplicationTypesForTask(applicationTask);

    return taskAppTypes.contains(pwaApplicationDetail.getPwaApplicationType())
        && getTaskService(applicationTask).allowCopyOfSectionInformation(pwaApplicationDetail);

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
  public boolean isTaskComplete(GeneralPurposeApplicationTask applicationTask, PwaApplicationDetail pwaApplicationDetail) {
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

  private Set<PwaResourceType> getValidResourceTypesForTask(GeneralPurposeApplicationTask applicationTask) {
    return Optional.ofNullable(applicationTask.getControllerClass())
        // this allows us to test method logic by returning an arbitrary class from the applicationContext in tests
        .map(controllerClass -> applicationContext.getBean(controllerClass).getClass())
        .map(clazz -> clazz.getAnnotation(PwaResourceTypeCheck.class))
        .map(typeCheck -> Set.of(typeCheck.types()))
        // task has valid resource type if controller has no type restriction
        .orElse(EnumSet.allOf(PwaResourceType.class));
  }

}
