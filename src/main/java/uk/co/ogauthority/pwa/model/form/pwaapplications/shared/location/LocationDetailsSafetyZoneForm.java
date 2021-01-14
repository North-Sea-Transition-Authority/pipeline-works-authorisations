package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

public class LocationDetailsSafetyZoneForm {

  private List<String> facilities = new ArrayList<>();
  private Boolean psrNotificationSubmitted;
  private TwoFieldDateInput psrNotificationSubmittedDate;
  private TwoFieldDateInput psrNotificationExpectedSubmissionDate;




  public List<String> getFacilities() {
    return facilities;
  }

  public void setFacilities(List<String> facilities) {
    this.facilities = facilities;
  }

  public Boolean getPsrNotificationSubmitted() {
    return psrNotificationSubmitted;
  }

  public void setPsrNotificationSubmitted(Boolean psrNotificationSubmitted) {
    this.psrNotificationSubmitted = psrNotificationSubmitted;
  }

  public TwoFieldDateInput getPsrNotificationSubmittedDate() {
    return psrNotificationSubmittedDate;
  }

  public void setPsrNotificationSubmittedDate(
      TwoFieldDateInput psrNotificationSubmittedDate) {
    this.psrNotificationSubmittedDate = psrNotificationSubmittedDate;
  }

  public TwoFieldDateInput getPsrNotificationExpectedSubmissionDate() {
    return psrNotificationExpectedSubmissionDate;
  }

  public void setPsrNotificationExpectedSubmissionDate(
      TwoFieldDateInput psrNotificationExpectedSubmissionDate) {
    this.psrNotificationExpectedSubmissionDate = psrNotificationExpectedSubmissionDate;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationDetailsSafetyZoneForm that = (LocationDetailsSafetyZoneForm) o;

    return Objects.equals(facilities, that.facilities)
        && Objects.equals(psrNotificationSubmitted, that.psrNotificationSubmitted)
        && Objects.equals(psrNotificationSubmittedDate, that.psrNotificationSubmittedDate)
        && Objects.equals(psrNotificationExpectedSubmissionDate, that.psrNotificationExpectedSubmissionDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(facilities, psrNotificationSubmitted, psrNotificationSubmittedDate, psrNotificationExpectedSubmissionDate);
  }
}
