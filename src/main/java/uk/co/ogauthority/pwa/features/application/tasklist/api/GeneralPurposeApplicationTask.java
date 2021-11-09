package uk.co.ogauthority.pwa.features.application.tasklist.api;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;

/**
 * Provides an interface understood by the {@see TaskListEntryFactory} for implementations of tasks within a task list.
 */
public interface GeneralPurposeApplicationTask {

  /**
   * Returns the class of a service which should control application specific task behaviour.
   */
  Class<? extends ApplicationFormSectionService> getServiceClass();

  /**
   * Returns the class of a controller that defines general purpose task restrictions.
   */
  Class getControllerClass();

  int getDisplayOrder();

  String getDisplayName();

  String getShortenedDisplayName();

  String getTaskLandingPageRoute(PwaApplication pwaApplication);


}
