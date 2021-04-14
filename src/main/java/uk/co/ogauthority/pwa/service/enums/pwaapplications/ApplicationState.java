package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.CASE_OFFICER_REVIEW;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.COMPLETE;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.DELETED;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.DRAFT;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.UPDATE_REQUESTED;
import static uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus.WITHDRAWN;

import java.util.EnumSet;
import java.util.Set;

/**
 * Top level state of the applications represented by individual statuses.
 */
public enum ApplicationState {

  REQUIRES_INDUSTRY_ATTENTION(DRAFT, UPDATE_REQUESTED, AWAITING_APPLICATION_PAYMENT),

  INDUSTRY_EDITABLE(DRAFT, UPDATE_REQUESTED),

  IN_PROGRESS(DRAFT, UPDATE_REQUESTED, INITIAL_SUBMISSION_REVIEW, AWAITING_APPLICATION_PAYMENT, CASE_OFFICER_REVIEW),

  SUBMITTED(UPDATE_REQUESTED, INITIAL_SUBMISSION_REVIEW, AWAITING_APPLICATION_PAYMENT, CASE_OFFICER_REVIEW),

  COMPLETED(COMPLETE, WITHDRAWN),

  DELETED_PRE_SUBMIT(DELETED),

  ENDED(COMPLETE, WITHDRAWN, DELETED);

  private final Set<PwaApplicationStatus> statuses;

  ApplicationState(PwaApplicationStatus... statuses) {
    this.statuses = EnumSet.copyOf(Set.of(statuses));
  }

  public Set<PwaApplicationStatus> getStatuses() {
    return statuses;
  }

  public boolean includes(PwaApplicationStatus status) {
    return statuses.contains(status);
  }

}
