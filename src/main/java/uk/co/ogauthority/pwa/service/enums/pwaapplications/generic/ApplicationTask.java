package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.FastTrackController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;

/**
 * Enumeration of all app form tasks for the PWA application task list.
 */
public enum ApplicationTask {

  PROJECT_INFORMATION("Project information", ProjectInformationController.class, 10),
  FAST_TRACK("Fast-track", FastTrackController.class, 20),
  ENVIRONMENTAL_DECOMMISSIONING("Environmental and decommissioning", EnvironmentalDecomController.class, 30),
  CROSSING_AGREEMENTS("Crossing agreements", CrossingAgreementsController.class, 40);

  private String displayName;
  private Class<?> controllerClass;
  private int displayOrder;

  ApplicationTask(String displayName, Class<?> controllerClass, int displayOrder) {
    this.displayName = displayName;
    this.controllerClass = controllerClass;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Class<?> getControllerClass() {
    return controllerClass;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<ApplicationTask> stream() {
    return Stream.of(ApplicationTask.values());
  }
}
