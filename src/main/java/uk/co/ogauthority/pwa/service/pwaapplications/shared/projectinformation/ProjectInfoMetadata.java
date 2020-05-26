package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;

/* Hold information commonly looked referenced in business logic */
public final class ProjectInfoMetadata {
  private final LocalDate proposedStartDate;

  private final Boolean campaignApproachBeingUsed;

  private ProjectInfoMetadata(LocalDate proposedStartDate,
                              Boolean campaignApproachBeingUsed) {
    this.proposedStartDate = proposedStartDate;
    this.campaignApproachBeingUsed = campaignApproachBeingUsed;
  }

  static ProjectInfoMetadata from(PadProjectInformation padProjectInformation) {
    if (padProjectInformation == null) {
      return new ProjectInfoMetadata(null, null);
    }

    return new ProjectInfoMetadata(
        padProjectInformation.getProposedStartTimestamp() != null
            ? padProjectInformation.getProposedStartTimestamp().atZone(ZoneId.systemDefault()).toLocalDate() : null,
        padProjectInformation.getUsingCampaignApproach()
    );
  }

  public Optional<LocalDate> getProposedStartDate() {
    return Optional.ofNullable(proposedStartDate);
  }

  public Boolean getCampaignApproachBeingUsed() {
    return campaignApproachBeingUsed;
  }
}
