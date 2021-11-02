package uk.co.ogauthority.pwa.model.entity.files;

import uk.co.ogauthority.pwa.controller.files.PwaApplicationDetailDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.supplementarydocs.SupplementaryDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.controller.CableCrossingDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.controller.BlockCrossingDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.controller.MedianLineDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline.controller.PipelineCrossingDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.controller.LocationDetailsController;
import uk.co.ogauthority.pwa.features.application.tasks.optionstemplate.controller.OptionsTemplateController;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.controller.PartnerLettersController;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.controller.AdmiraltyChartDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.controller.PipelineDrawingController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.controller.UmbilicalCrossSectionDocumentsController;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller.ProjectInformationController;

/**
 * Enumeration of the different areas of a PWA application detail that can have file links.
 */
public enum ApplicationDetailFilePurpose {

  LOCATION_DETAILS(LocationDetailsController.class),
  PROJECT_INFORMATION(ProjectInformationController.class),
  BLOCK_CROSSINGS(BlockCrossingDocumentsController.class),
  CABLE_CROSSINGS(CableCrossingDocumentsController.class),
  PIPELINE_CROSSINGS(PipelineCrossingDocumentsController.class),
  MEDIAN_LINE_CROSSING(MedianLineDocumentsController.class),
  PIPELINE_DRAWINGS(PipelineDrawingController.class),
  DEPOSIT_DRAWINGS(PermanentDepositDrawingsController.class),
  PARTNER_LETTERS(PartnerLettersController.class),
  UMBILICAL_CROSS_SECTION(UmbilicalCrossSectionDocumentsController.class),
  ADMIRALTY_CHART(AdmiraltyChartDocumentsController.class),

  OPTIONS_TEMPLATE(OptionsTemplateController.class),

  SUPPLEMENTARY_DOCUMENTS(SupplementaryDocumentsController.class);

  private final Class<? extends PwaApplicationDetailDataFileUploadAndDownloadController> fileControllerClass;

  ApplicationDetailFilePurpose(
      Class<? extends PwaApplicationDetailDataFileUploadAndDownloadController> fileControllerClass) {
    this.fileControllerClass = fileControllerClass;
  }

  public Class<? extends PwaApplicationDetailDataFileUploadAndDownloadController> getFileControllerClass() {
    return fileControllerClass;
  }

}
