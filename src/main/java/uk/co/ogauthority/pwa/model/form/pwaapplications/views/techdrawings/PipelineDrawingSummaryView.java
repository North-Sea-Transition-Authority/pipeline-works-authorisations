package uk.co.ogauthority.pwa.model.form.pwaapplications.views.techdrawings;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public class PipelineDrawingSummaryView {

  private final String reference;
  private final String documentDescription;
  private final String fileId;
  private final String fileName;
  private final List<PipelineOverview> pipelineOverviews;
  private final Integer drawingId;

  public PipelineDrawingSummaryView(PadTechnicalDrawing technicalDrawing, List<PipelineOverview> pipelineOverviews,
                                    UploadedFileView uploadedFileView) {
    this.reference = technicalDrawing.getReference();
    this.documentDescription = uploadedFileView.getFileDescription();
    this.pipelineOverviews = pipelineOverviews;
    this.fileId = uploadedFileView.getFileId();
    this.fileName = uploadedFileView.getFileName();
    this.drawingId = technicalDrawing.getId();
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
    return fileName;
  }

  public List<PipelineOverview> getPipelineOverviews() {
    return pipelineOverviews;
  }

  public Integer getDrawingId() {
    return drawingId;
  }
}
