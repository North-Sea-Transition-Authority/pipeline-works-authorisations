package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumerates all the states that a PWA application can be in.
 */
public enum PwaApplicationStatus {

  DRAFT("Draft", ApplicationState.DRAFT),
  INITIAL_SUBMISSION_REVIEW("Submitted - awaiting review", ApplicationState.SUBMITTED),
  AWAITING_APPLICATION_PAYMENT("Submitted - awaiting payment", ApplicationState.SUBMITTED),
  CASE_OFFICER_REVIEW("Case officer review", ApplicationState.SUBMITTED),
  WITHDRAWN("Withdrawn", ApplicationState.COMPLETED),
  DELETED("Deleted", ApplicationState.DELETED),
  COMPLETE("Complete", ApplicationState.COMPLETED);

  private final String displayName;
  private final ApplicationState applicationState;

  PwaApplicationStatus(String displayName,
                       ApplicationState applicationState) {
    this.displayName = displayName;
    this.applicationState = applicationState;
  }


  public String getDisplayName() {
    return displayName;
  }

  /**
   * Return application statuses where the associated state matches any of the param states.
   */
  public static Set<PwaApplicationStatus> getStatusesWithState(ApplicationState... states) {
    var statesFilter = EnumSet.copyOf(Arrays.asList(states));
    return Arrays.stream(PwaApplicationStatus.values())
        .filter(pwaApplicationStatus -> statesFilter.contains(pwaApplicationStatus.applicationState))
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(PwaApplicationStatus.class)));
  }

}
