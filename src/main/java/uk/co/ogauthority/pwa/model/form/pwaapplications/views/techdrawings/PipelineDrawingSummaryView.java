package uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public class PipelineDrawingSummaryView {

  private final String reference;
  private final String documentDescription;
  private final String fileId;
  private final String fileName;
  private final List<PipelineOverview> pipelineOverviews;

  public PipelineDrawingSummaryView(PadTechnicalDrawing technicalDrawing, List<PipelineOverview> pipelineOverviews) {
    this.reference = technicalDrawing.getReference();
    this.documentDescription = technicalDrawing.getFileDescription();
    this.pipelineOverviews = pipelineOverviews;
    this.fileId = technicalDrawing.getFileId();
    this.fileName = technicalDrawing.getFileId()
  }

  public String getReference() {
    return reference;
  }

  public String getDocumentDescription() {
    return documentDescription;
  }

  public String getFileId() {
    return fileId;
  }

  public String getFileName() {
    return file
  }

  public List<PipelineOverview> getPipelineOverviews() {
    return pipelineOverviews;
  }
}
