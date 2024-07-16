package uk.co.ogauthority.pwa.model.entity.asbuilt;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity
@Table(name = "as_built_notif_grp_status_hist")
public class AsBuiltNotificationGroupStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "asBuiltNotificationGroupId")
  private AsBuiltNotificationGroup asBuiltNotificationGroup;

  @Enumerated(EnumType.STRING)
  private AsBuiltNotificationGroupStatus status;

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


  public AsBuiltNotificationGroupStatusHistory() {
    // hibernate
  }

  public AsBuiltNotificationGroupStatusHistory(AsBuiltNotificationGroup asBuiltNotificationGroup,
                                               AsBuiltNotificationGroupStatus status,
                                               PersonId createdByPersonId,
                                               Instant createdTimestamp) {
    this.asBuiltNotificationGroup = asBuiltNotificationGroup;
    this.status = status;
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

  public void setStatus(AsBuiltNotificationGroupStatus status) {
    this.status = status;
  }

  public AsBuiltNotificationGroupStatus getStatus() {
    return status;
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
