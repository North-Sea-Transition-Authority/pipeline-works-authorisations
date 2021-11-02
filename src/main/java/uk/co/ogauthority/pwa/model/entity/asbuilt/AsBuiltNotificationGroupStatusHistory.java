package uk.co.ogauthority.pwa.model.entity.asbuilt;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
