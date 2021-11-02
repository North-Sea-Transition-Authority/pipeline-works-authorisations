package uk.co.ogauthority.pwa.model.view.appprocessing.options;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.util.DateUtils;

public class OptionsApprovalDeadlineView {

  private final PersonId approvedByPersonId;
  private final Instant approvedInstant;

  private final PersonId deadlineUpdatedByPersonId;
  private final Instant deadlineUpdatedInstant;

  private final Instant deadlineInstant;

  // avoid creating this as local date every time its required and just do it once in constructor.
  private final LocalDate deadlineLocalDate;

  private final String updateNote;

  public OptionsApprovalDeadlineView(PersonId approvedByPersonId,
                                     Instant approvedInstant,
                                     PersonId deadlineUpdatedByPersonId,
                                     Instant deadlineUpdatedInstant,
                                     Instant deadlineInstant,
                                     String updateNote) {
    this.approvedByPersonId = approvedByPersonId;
    this.approvedInstant = approvedInstant;
    this.deadlineUpdatedByPersonId = deadlineUpdatedByPersonId;
    this.deadlineUpdatedInstant = deadlineUpdatedInstant;
    this.deadlineInstant = deadlineInstant;
    this.deadlineLocalDate = DateUtils.instantToLocalDate(this.deadlineInstant);
    this.updateNote = updateNote;
  }

  public PersonId getApprovedByPersonId() {
    return approvedByPersonId;
  }

  public Instant getApprovedInstant() {
    return approvedInstant;
  }

  public PersonId getDeadlineUpdatedByPersonId() {
    return deadlineUpdatedByPersonId;
  }

  public Instant getDeadlineUpdatedInstant() {
    return deadlineUpdatedInstant;
  }

  public Instant getDeadlineInstant() {
    return deadlineInstant;
  }

  public String getUpdateNote() {
    return updateNote;
  }

  public LocalDate getDeadlineLocalDate() {
    return deadlineLocalDate;
  }
}
