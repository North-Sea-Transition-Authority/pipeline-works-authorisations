package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails;

import java.util.List;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class PipelineDrawingForm extends UploadMultipleFilesWithDescriptionForm {

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
