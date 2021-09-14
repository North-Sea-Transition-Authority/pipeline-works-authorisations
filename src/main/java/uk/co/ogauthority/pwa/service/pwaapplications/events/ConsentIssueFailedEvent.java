package uk.co.ogauthority.pwa.service.pwaapplications.events;

import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class ConsentIssueFailedEvent {

  private final PwaApplicationDetail pwaApplicationDetail;
  private final Exception exception;
  private final WebUserAccount issuingUser;

  public ConsentIssueFailedEvent(PwaApplicationDetail pwaApplicationDetail,
                                 Exception exception,
                                 WebUserAccount issuingUser) {
    this.pwaApplicationDetail = pwaApplicationDetail;
    this.exception = exception;
    this.issuingUser = issuingUser;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return pwaApplicationDetail;
  }

  public Exception getException() {
    return exception;
  }

  public WebUserAccount getIssuingUser() {
    return issuingUser;
  }

}
