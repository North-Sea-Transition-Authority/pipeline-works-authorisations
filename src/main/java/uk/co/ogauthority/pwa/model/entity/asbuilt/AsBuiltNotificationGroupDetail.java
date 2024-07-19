package uk.co.ogauthority.pwa.model.entity.asbuilt;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity
@Table(name = "as_built_notif_grp_details")
public class AsBuiltNotificationGroupDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "asBuiltNotificationGroupId")
  private AsBuiltNotificationGroup asBuiltNotificationGroup;

  private LocalDate deadlineDate;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "created_by_person_id")
  private PersonId createdByPersonId;
  private Instant createdTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "ended_by_person_id")
  private PersonId endedByPersonId;
  private Instant endedTimestamp;


  public AsBuiltNotificationGroupDetail() {
    //hibernate
  }

  public AsBuiltNotificationGroupDetail(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                        LocalDate deadlineDate,
                                        PersonId createdByPersonId,
                                        Instant createdTimestamp) {
    this.asBuiltNotificationGroup = asBuiltNotificationGroup;
    this.deadlineDate = deadlineDate;
    this.createdByPersonId = createdByPersonId;
    this.createdTimestamp = createdTimestamp;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AsBuiltNotificationGroup getAsBuiltNotificationGroup() {
    return asBuiltNotificationGroup;
  }

  public void setAsBuiltNotificationGroup(
      AsBuiltNotificationGroup asBuiltNotificationGroup) {
    this.asBuiltNotificationGroup = asBuiltNotificationGroup;
  }

  public LocalDate getDeadlineDate() {
    return deadlineDate;
  }

  public void setDeadlineDate(LocalDate deadlineDate) {
    this.deadlineDate = deadlineDate;
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

  public PersonId getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(PersonId endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }

  public Instant getEndedTimestamp() {
    return endedTimestamp;
  }

  public void setEndedTimestamp(Instant endedTimestamp) {
    this.endedTimestamp = endedTimestamp;
  }
}
