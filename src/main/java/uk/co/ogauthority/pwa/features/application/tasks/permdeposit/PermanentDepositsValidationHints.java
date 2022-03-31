package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PermanentDepositsValidationHints {

  private final PwaApplicationDetail applicationDetail;
  private final Instant projectInfoProposedStartTimestamp;
  private final List<PadPermanentDeposit> existingDepositsForApp;
  private final Set<String> acceptedPipelineIds;

  public PermanentDepositsValidationHints(
      PwaApplicationDetail applicationDetail,
      Instant projectInfoProposedStartTimestamp,
      List<PadPermanentDeposit> existingDepositsForApp,
      Set<String> acceptedPipelineIds
  ) {
    this.applicationDetail = applicationDetail;
    this.projectInfoProposedStartTimestamp = projectInfoProposedStartTimestamp;
    this.existingDepositsForApp = existingDepositsForApp;
    this.acceptedPipelineIds = acceptedPipelineIds;
  }


  public PwaApplicationDetail getApplicationDetail() {
    return applicationDetail;
  }

  Instant getProjectInfoProposedStartTimestamp() {
    return projectInfoProposedStartTimestamp;
  }

  List<PadPermanentDeposit> getExistingDepositsForApp() {
    return existingDepositsForApp;
  }

  public Set<String> getAcceptedPipelineIds() {
    return acceptedPipelineIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PermanentDepositsValidationHints that = (PermanentDepositsValidationHints) o;
    return Objects.equals(applicationDetail, that.applicationDetail)
        && Objects.equals(projectInfoProposedStartTimestamp, that.projectInfoProposedStartTimestamp)
        && Objects.equals(existingDepositsForApp, that.existingDepositsForApp)
        && Objects.equals(acceptedPipelineIds, that.acceptedPipelineIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationDetail, projectInfoProposedStartTimestamp, existingDepositsForApp, acceptedPipelineIds);
  }
}
