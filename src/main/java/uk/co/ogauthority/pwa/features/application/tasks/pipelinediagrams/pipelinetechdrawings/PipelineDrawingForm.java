package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import java.util.List;
import uk.co.ogauthority.pwa.features.filemanagement.FileUploadForm;

public class PipelineDrawingForm extends FileUploadForm {

  private String reference;
  private List<Integer> padPipelineIds;

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public List<Integer> getPadPipelineIds() {
    return padPipelineIds;
  }

  public void setPadPipelineIds(List<Integer> padPipelineIds) {
    this.padPipelineIds = padPipelineIds;
  }
}
