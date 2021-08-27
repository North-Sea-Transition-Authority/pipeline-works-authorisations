package uk.co.ogauthority.pwa.validators.deposits;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;

public class PermanentDepositsValidationHints {

  private final PwaApplicationDetail applicationDetail;
  private final Instant projectInfoProposedStartTimestamp;
  private final List<PadPermanentDeposit> existingDepositsForApp;

  public PermanentDepositsValidationHints(
      PwaApplicationDetail applicationDetail, Instant projectInfoProposedStartTimestamp,
      List<PadPermanentDeposit> existingDepositsForApp) {
    this.applicationDetail = applicationDetail;
    this.projectInfoProposedStartTimestamp = projectInfoProposedStartTimestamp;
    this.existingDepositsForApp = existingDepositsForApp;
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
        && Objects.equals(existingDepositsForApp, that.existingDepositsForApp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationDetail, projectInfoProposedStartTimestamp, existingDepositsForApp);
  }
}
