package uk.co.ogauthority.pwa.model.entity.files;

import uk.co.ogauthority.pwa.controller.appprocessing.casenotes.CaseNoteController;
import uk.co.ogauthority.pwa.controller.consultations.responses.ConsultationResponseFileController;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeDraftController;

/**
 * Enumeration of the different areas of a PWA application that can have file links.
 */
public enum AppFilePurpose {

  CASE_NOTES(CaseNoteController.class),
  PUBLIC_NOTICE(PublicNoticeDraftController.class),
  CONSULTATION_RESPONSE(ConsultationResponseFileController.class);

  private final Class<? extends PwaApplicationDataFileUploadAndDownloadController> fileControllerClass;

  AppFilePurpose(
      Class<? extends PwaApplicationDataFileUploadAndDownloadController> fileControllerClass) {
    this.fileControllerClass = fileControllerClass;
  }

  public Class<? extends PwaApplicationDataFileUploadAndDownloadController> getFileControllerClass() {
    return fileControllerClass;
  }

}
