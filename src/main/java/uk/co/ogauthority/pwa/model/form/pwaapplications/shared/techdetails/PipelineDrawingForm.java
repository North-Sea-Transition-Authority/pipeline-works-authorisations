package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails;

import java.util.List;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class PipelineDrawingForm extends UploadMultipleFilesWithDescriptionForm {

  private String reference;
  private List<Integer> pipelineIds;

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public List<Integer> getPipelineIds() {
    return pipelineIds;
  }

  public void setPipelineIds(List<Integer> pipelineIds) {
    this.pipelineIds = pipelineIds;
  }
}
