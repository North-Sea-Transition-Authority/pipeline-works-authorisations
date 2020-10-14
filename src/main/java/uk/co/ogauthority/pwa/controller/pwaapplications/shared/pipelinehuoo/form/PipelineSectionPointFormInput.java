package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form;

import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption;

/**
 * Designed to be used within a list which defines the path of points upon which a pipeline can be split between HUOO roles.
 */
public class PipelineSectionPointFormInput {

  private String pickedPipelineIdentString;
  private Boolean pointIncludedInSection;

  public PipelineSectionPointFormInput() {
  }

  public PipelineSectionPointFormInput(String pickedPipelineIdentString, Boolean pointIncludedInSection) {
    this.pickedPipelineIdentString = pickedPipelineIdentString;
    this.pointIncludedInSection = pointIncludedInSection;
  }

  public static PipelineSectionPointFormInput createFirstSectionPoint(
      PickableIdentLocationOption pickableIdentLocationOption) {
    var input = new PipelineSectionPointFormInput();
    input.setPickedPipelineIdentString(pickableIdentLocationOption.getPickableString());
    input.setPointIncludedInSection(true);
    return input;
  }

  public String getPickedPipelineIdentString() {
    return pickedPipelineIdentString;
  }

  public void setPickedPipelineIdentString(String pickedPipelineIdentString) {
    this.pickedPipelineIdentString = pickedPipelineIdentString;
  }

  public Boolean getPointIncludedInSection() {
    return pointIncludedInSection;
  }

  public void setPointIncludedInSection(Boolean pointIncludedInSection) {
    this.pointIncludedInSection = pointIncludedInSection;
  }

}
