package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;

public class PadTechnicalDrawingValidationHints {

  private final PwaApplicationDetail pwaApplicationDetail;
  private final PadTechnicalDrawing existingDrawing;
  private final PipelineDrawingValidationType validationType;


  public PadTechnicalDrawingValidationHints(
      PwaApplicationDetail pwaApplicationDetail,
      PadTechnicalDrawing existingDrawing,
      PipelineDrawingValidationType validationType) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.existingDrawing = existingDrawing;
    this.validationType = validationType;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public PadTechnicalDrawing getExistingDrawing() {
    return existingDrawing;
  }

  public PipelineDrawingValidationType getValidationType() {
    return validationType;
  }




}
