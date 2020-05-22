package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.FastTrackController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.HuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PermanentDepositController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.TechnicalDrawingsService;

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

  LOCATION_DETAILS(
      "Location details",
      LocationDetailsController.class,
      PadLocationDetailsService.class,
      30),

  ENVIRONMENTAL_DECOMMISSIONING(
      "Environmental and decommissioning",
      EnvironmentalDecomController.class,
      PadEnvironmentalDecommissioningService.class,
      40),

  HUOO(
      "Holders, users, operators, and owners",
      HuooController.class,
      PadOrganisationRoleService.class,
      50),

  CROSSING_AGREEMENTS(
      "Blocks and crossing agreements",
      CrossingAgreementsController.class,
      CrossingAgreementsService.class,
      60),

  PIPELINES(
    "Pipelines",
      PipelinesController.class,
      PadPipelineService.class,
      70
  ),

  TECHNICAL_DRAWINGS(
      "Admiralty chart and pipeline drawings",
      TechnicalDrawingsController.class,
      TechnicalDrawingsService.class,
      80
  ),

  PERMANENT_DEPOSITS(
    "Permanent deposits",
    PermanentDepositController.class,
    PermanentDepositService.class,
      90
  );



  private final String displayName;
  private final Class<?> controllerClass;
  private final Class<? extends ApplicationFormSectionService> serviceClass;
  private final int displayOrder;

  ApplicationTask(String displayName, Class<?> controllerClass,
                  Class<? extends ApplicationFormSectionService> serviceClass,
                  int displayOrder) {
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

  public Class<? extends ApplicationFormSectionService> getServiceClass() {
    return serviceClass;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<ApplicationTask> stream() {
    return Stream.of(ApplicationTask.values());
  }
}
