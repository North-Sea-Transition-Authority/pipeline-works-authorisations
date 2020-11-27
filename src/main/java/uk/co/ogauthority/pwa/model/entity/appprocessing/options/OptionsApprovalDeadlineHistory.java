package uk.co.ogauthority.pwa.model.entity.appprocessing.options;

import java.time.Clock;
import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;


@Entity
@Table(name = "options_app_appr_deadline_hist")
public class OptionsApprovalDeadlineHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "options_app_approval_id")
  private OptionsApplicationApproval optionsApplicationApproval;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "created_by_person_id")
  private PersonId createdByPersonId;

  private Instant createdTimestamp;

  private Instant deadlineDate;

  private String note;

  private Boolean tipFlag;

  public static OptionsApprovalDeadlineHistory createInitialTipFrom(
      OptionsApplicationApproval optionsApplicationApproval,
      Instant deadlineDate,
      String note) {
    var history = new OptionsApprovalDeadlineHistory();
    history.setCreatedByPersonId(optionsApplicationApproval.getCreatedByPersonId());
    history.setCreatedTimestamp(optionsApplicationApproval.getCreatedTimestamp());
    history.setOptionsApplicationApproval(optionsApplicationApproval);
    history.setTipFlag(true);
    history.setDeadlineDate(deadlineDate);
    history.setNote(note);
    return history;

  }

  public static OptionsApprovalDeadlineHistory createTipFrom(OptionsApplicationApproval optionsApplicationApproval,
                                                             PersonId createdByPersonId,
                                                             Clock clock,
                                                             Instant deadlineDate,
                                                             String note) {
    var history = new OptionsApprovalDeadlineHistory();
    history.setCreatedByPersonId(createdByPersonId);
    history.setCreatedTimestamp(clock.instant());
    history.setOptionsApplicationApproval(optionsApplicationApproval);
    history.setTipFlag(true);
    history.setDeadlineDate(deadlineDate);
    history.setNote(note);
    return history;

  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public OptionsApplicationApproval getOptionsApplicationApproval() {
    return optionsApplicationApproval;
  }

  public void setOptionsApplicationApproval(
      OptionsApplicationApproval optionsAppApprovalId) {
    this.optionsApplicationApproval = optionsAppApprovalId;
  }

  public PersonId getCreatedByPersonId() {
    return createdByPersonId;
  }

  public void setCreatedByPersonId(PersonId createdByPersonId) {
    this.createdByPersonId = createdByPersonId;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public Instant getDeadlineDate() {
    return deadlineDate;
  }

  public void setDeadlineDate(Instant deadlineDate) {
    this.deadlineDate = deadlineDate;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public boolean isTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(boolean tipFlag) {
    this.tipFlag = tipFlag;
  }
}
