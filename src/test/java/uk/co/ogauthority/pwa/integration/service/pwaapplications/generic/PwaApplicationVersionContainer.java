package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;

/**
 * Test code only container designed to holder references to each entity linked to a single version of an application.
 */
public class PwaApplicationVersionContainer {
  private final PwaApplicationDetail pwaApplicationDetail;

  private PadProjectInformation padProjectInformation;

  private Map<ApplicationFilePurpose, PadFile> padFiles;

  private PadTechnicalDrawing padTechnicalDrawing;

  private PadTechnicalDrawingLink padTechnicalDrawingLink;

  private SimplePadPipelineContainer simplePadPipelineContainer;

  public PwaApplicationVersionContainer(
      PwaApplicationDetail pwaApplicationDetail) {
    this.pwaApplicationDetail = pwaApplicationDetail;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public PadProjectInformation getPadProjectInformation() {
    return padProjectInformation;
  }

  public void setPadProjectInformation(
      PadProjectInformation padProjectInformation) {
    this.padProjectInformation = padProjectInformation;
  }

  public PadFile getPadFile(ApplicationFilePurpose applicationFilePurpose) {
    return this.padFiles.get(applicationFilePurpose);
  }

  public void setPadFiles(Collection<PadFile> padFiles) {
    this.padFiles = padFiles.stream()
        .collect(Collectors.toMap(PadFile::getPurpose, padFile -> padFile));
  }

  public SimplePadPipelineContainer getSimplePadPipelineContainer() {
    return simplePadPipelineContainer;
  }

  public void setSimplePadPipelineContainer(
      SimplePadPipelineContainer simplePadPipelineContainer) {
    this.simplePadPipelineContainer = simplePadPipelineContainer;
  }

  public PadTechnicalDrawing getPadTechnicalDrawing() {
    return padTechnicalDrawing;
  }

  public void setPadTechnicalDrawing(
      PadTechnicalDrawing padTechnicalDrawing) {
    this.padTechnicalDrawing = padTechnicalDrawing;
  }

  public PadTechnicalDrawingLink getPadTechnicalDrawingLink() {
    return padTechnicalDrawingLink;
  }

  public void setPadTechnicalDrawingLink(
      PadTechnicalDrawingLink padTechnicalDrawingLink) {
    this.padTechnicalDrawingLink = padTechnicalDrawingLink;
  }
}
