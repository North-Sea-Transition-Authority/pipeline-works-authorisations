package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.FastTrackController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;

/**
 * Enumeration of all app form tasks for the PWA application task list.
 */
public enum ApplicationTask {

  PROJECT_INFORMATION(
      "Project information",
      ProjectInformationController.class,
      PadProjectInformationService.class,
      10),

  FAST_TRACK(
      "Fast-track",
      FastTrackController.class,
      PadFastTrackService.class,
      20),

  ENVIRONMENTAL_DECOMMISSIONING(
      "Environmental and decommissioning",
      EnvironmentalDecomController.class,
      PadEnvironmentalDecommissioningService.class,
      30),

  CROSSING_AGREEMENTS(
      "Crossing agreements",
      CrossingAgreementsController.class,
      CrossingAgreementsService.class,
      40);



  private String displayName;
  private Class<?> controllerClass;
  private Class<?> serviceClass;
  private int displayOrder;

  ApplicationTask(String displayName, Class<?> controllerClass, Class<?> serviceClass, int displayOrder) {
    this.displayName = displayName;
    this.controllerClass = controllerClass;
    this.serviceClass = serviceClass;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Class<?> getControllerClass() {
    return controllerClass;
  }

  public Class<?> getServiceClass() {
    return serviceClass;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<ApplicationTask> stream() {
    return Stream.of(ApplicationTask.values());
  }
}
