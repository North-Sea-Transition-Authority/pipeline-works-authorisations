package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.List;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;

public class PipelineDrawingValidationFactory {

  private List<PipelineDrawingSummaryView> pipelineDrawingSummaryViews;

  public PipelineDrawingValidationFactory(
      List<PipelineDrawingSummaryView> pipelineDrawingSummaryViews) {
    this.pipelineDrawingSummaryViews = pipelineDrawingSummaryViews;
  }

  public boolean isValid(PipelineDrawingSummaryView summaryView) {
    return summaryView.getFileId() != null;
  }

  public String getErrorMessage(PipelineDrawingSummaryView summaryView) {
    if (summaryView.getFileId() == null) {
      return "This drawing is missing a file";
    }
    return null;
  }
}
