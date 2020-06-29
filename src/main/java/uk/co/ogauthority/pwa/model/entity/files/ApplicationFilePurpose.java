package uk.co.ogauthority.pwa.model.entity.files;

import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.BlockCrossingDocumentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CableCrossingDocumentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.MedianLineDocumentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.PipelineCrossingDocumentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.PipelineDrawingController;

/**
 * Enumeration of the different areas of a PWA application that can have file links.
 */
public enum ApplicationFilePurpose {

  LOCATION_DETAILS(LocationDetailsController.class),
  PROJECT_INFORMATION(ProjectInformationController.class),
  BLOCK_CROSSINGS(BlockCrossingDocumentsController.class),
  CABLE_CROSSINGS(CableCrossingDocumentsController.class),
  PIPELINE_CROSSINGS(PipelineCrossingDocumentsController.class),
  MEDIAN_LINE_CROSSING(MedianLineDocumentsController.class),
  PIPELINE_DRAWINGS(PipelineDrawingController.class),
  DEPOSIT_DRAWINGS(PermanentDepositDrawingsController.class);

  private final Class<? extends PwaApplicationDataFileUploadAndDownloadController> fileControllerClass;

  ApplicationFilePurpose(
      Class<? extends PwaApplicationDataFileUploadAndDownloadController> fileControllerClass) {
    this.fileControllerClass = fileControllerClass;
  }

  public Class<? extends PwaApplicationDataFileUploadAndDownloadController> getFileControllerClass() {
    return fileControllerClass;
  }

}
