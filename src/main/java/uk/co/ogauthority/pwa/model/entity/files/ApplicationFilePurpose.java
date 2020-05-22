package uk.co.ogauthority.pwa.model.entity.files;

import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.PipelineDrawingController;

/**
 * Enumeration of the different areas of a PWA application that can have file links.
 */
public enum ApplicationFilePurpose {

  LOCATION_DETAILS(LocationDetailsController.class),
  PROJECT_INFORMATION(ProjectInformationController.class),
  BLOCK_CROSSINGS(null),
  CABLE_CROSSINGS(null),
  PIPELINE_CROSSINGS(null),
  MEDIAN_LINE_CROSSING(null),
  PIPELINE_DRAWINGS(PipelineDrawingController.class);

  private final Class<? extends PwaApplicationDataFileUploadAndDownloadController> fileControllerClass;

  ApplicationFilePurpose(
      Class<? extends PwaApplicationDataFileUploadAndDownloadController> fileControllerClass) {
    this.fileControllerClass = fileControllerClass;
  }

  public Class<? extends PwaApplicationDataFileUploadAndDownloadController> getFileControllerClass() {
    return fileControllerClass;
  }

}
