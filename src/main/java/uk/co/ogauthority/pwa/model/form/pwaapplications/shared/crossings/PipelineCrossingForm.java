package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings;

import java.util.List;

public class PipelineCrossingForm {

  private String pipelineCrossed;
  private Boolean pipelineFullyOwnedByOrganisation;
  private List<String> pipelineOwners;

  public String getPipelineCrossed() {
    return pipelineCrossed;
  }

  public void setPipelineCrossed(String pipelineCrossed) {
    this.pipelineCrossed = pipelineCrossed;
  }

  public Boolean getPipelineFullyOwnedByOrganisation() {
    return pipelineFullyOwnedByOrganisation;
  }

  public void setPipelineFullyOwnedByOrganisation(Boolean pipelineFullyOwnedByOrganisation) {
    this.pipelineFullyOwnedByOrganisation = pipelineFullyOwnedByOrganisation;
  }

  public List<String> getPipelineOwners() {
    return pipelineOwners;
  }

  public void setPipelineOwners(List<String> pipelineOwners) {
    this.pipelineOwners = pipelineOwners;
  }
}
