package uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public class PipelineDrawingSummaryView {

  private String reference;
  private String documentDescription;
  private List<PipelineOverview> pipelineOverviews;

  public PipelineDrawingSummaryView(PadTechnicalDrawing technicalDrawing, List<PipelineOverview> pipelineOverviews) {
    this.reference = technicalDrawing.getReference();
    this.documentDescription = technicalDrawing.getFileDescription();
    this.pipelineOverviews = pipelineOverviews;
  }

  public String getReference() {
    return reference;
  }

  public String getDocumentDescription() {
    return documentDescription;
  }

  public List<PipelineOverview> getPipelineOverviews() {
    return pipelineOverviews;
  }
}
