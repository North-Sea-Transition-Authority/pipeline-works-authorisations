package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.List;
import java.util.function.Function;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings.PipelineDrawingSummaryView;

public class PipelineDrawingValidationFactory {

  private final boolean isComplete;
  private final List<PadTechnicalDrawing> drawingList;
  private final Function<PadTechnicalDrawing, Boolean> drawingValidFunction;
  private final Function<PadTechnicalDrawing, String> getDrawingErrorMessageFunction;

  public PipelineDrawingValidationFactory(
      boolean isComplete,
      List<PadTechnicalDrawing> drawingList,
      Function<PadTechnicalDrawing, Boolean> drawingValidFunction,
      Function<PadTechnicalDrawing, String> getDrawingErrorMessageFunction) {
    this.isComplete = isComplete;
    this.drawingList = drawingList;
    this.drawingValidFunction = drawingValidFunction;
    this.getDrawingErrorMessageFunction = getDrawingErrorMessageFunction;
  }

  public boolean isValid(PipelineDrawingSummaryView summaryView) {
    return drawingValidFunction.apply(getDrawingFromSummary(summaryView));
  }

  public String getErrorMessage(PipelineDrawingSummaryView summaryView) {
    return getDrawingErrorMessageFunction.apply(getDrawingFromSummary(summaryView));
  }

  public boolean isComplete() {
    return isComplete;
  }

  private PadTechnicalDrawing getDrawingFromSummary(PipelineDrawingSummaryView summaryView) {
    return drawingList.stream()
        .filter(drawing1 -> drawing1.getId().equals(summaryView.getDrawingId()))
        .findFirst()
        .orElse(null);
  }
}
