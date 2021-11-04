package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;

public class PipelineDrawingSummaryView {

  private final String reference;
  private final String documentDescription;
  private final String fileId;
  private final String fileName;
  private final List<String> pipelineReferences;
  private final Integer drawingId;

  public PipelineDrawingSummaryView(PadTechnicalDrawing technicalDrawing, List<String> pipelineReferences) {
    this.reference = technicalDrawing.getReference();
    this.documentDescription = null;
    this.pipelineReferences = pipelineReferences;
    this.fileId = null;
    this.fileName = null;
    this.drawingId = technicalDrawing.getId();
  }

  public PipelineDrawingSummaryView(PadTechnicalDrawing technicalDrawing, List<String> pipelineReferences,
                                    UploadedFileView uploadedFileView) {
    this.reference = technicalDrawing.getReference();
    this.documentDescription = uploadedFileView.getFileDescription();
    this.pipelineReferences = pipelineReferences;
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

  public List<String> getPipelineReferences() {
    return pipelineReferences;
  }

  public Integer getDrawingId() {
    return drawingId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineDrawingSummaryView that = (PipelineDrawingSummaryView) o;
    return Objects.equals(reference, that.reference)
        && Objects.equals(documentDescription, that.documentDescription)
        && Objects.equals(fileId, that.fileId)
        && Objects.equals(fileName, that.fileName)
        && Objects.equals(pipelineReferences, that.pipelineReferences)
        && Objects.equals(drawingId, that.drawingId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reference, documentDescription, fileId, fileName, pipelineReferences, drawingId);
  }
}
