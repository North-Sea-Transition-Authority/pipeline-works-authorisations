package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

/**
 * Wrapper for the application tasks contained within a task group that allows custom ordering. Required so the group
 * can define the order of tasks as it might be different to the natural ordering of tasks if they were within the a
 * single combined list.
 */
public class OrderedTaskGroupTask implements GeneralPurposeApplicationTask {

  private final ApplicationTask applicationTask;
  private final int displayOrder;

  private OrderedTaskGroupTask(ApplicationTask applicationTask, int displayOrder) {
    this.applicationTask = applicationTask;
    this.displayOrder = displayOrder;
  }

  public static OrderedTaskGroupTask from(ApplicationTask applicationTask, int displayOrder) {
    return new OrderedTaskGroupTask(applicationTask, displayOrder);
  }

  @Override
  public Class<? extends ApplicationFormSectionService> getServiceClass() {
    return applicationTask.getServiceClass();
  }

  @Override
  public Class getControllerClass() {
    return applicationTask.getServiceClass();
  }

  @Override
  public int getDisplayOrder() {
    return this.displayOrder;
  }

  @Override
  public String getDisplayName() {
    return applicationTask.getDisplayName();
  }

  @Override
  public String getShortenedDisplayName() {
    return applicationTask.getShortenedDisplayName();
  }

  @Override
  public String getTaskLandingPageRoute(PwaApplication pwaApplication) {
    return applicationTask.getTaskLandingPageRoute(pwaApplication);
  }

  public ApplicationTask getApplicationTask() {
    return applicationTask;
  }
}
