package uk.co.ogauthority.pwa.model.entity.documents;

import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.Optional;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

@MappedSuperclass
public abstract class SectionClauseVersion {

  private Integer versionNo;

  private Boolean tipFlag;

  private String name;

  private String text;

  private Integer levelOrder;

  @Enumerated(EnumType.STRING)
  private SectionClauseVersionStatus status;

  private Instant createdTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId createdByPersonId;

  private Instant endedTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId endedByPersonId;

  public SectionClauseVersion() {
  }

  public Integer getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(Integer versionNo) {
    this.versionNo = versionNo;
  }

  public Boolean getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(Boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Integer getLevelOrder() {
    return levelOrder;
  }

  public void setLevelOrder(Integer levelOrder) {
    this.levelOrder = levelOrder;
  }

  public SectionClauseVersionStatus getStatus() {
    return status;
  }

  public void setStatus(SectionClauseVersionStatus status) {
    this.status = status;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public PersonId getCreatedByPersonId() {
    return createdByPersonId;
  }

  public void setCreatedByPersonId(PersonId createdByPersonId) {
    this.createdByPersonId = createdByPersonId;
  }

  public Instant getEndedTimestamp() {
    return endedTimestamp;
  }

  public void setEndedTimestamp(Instant endedTimestamp) {
    this.endedTimestamp = endedTimestamp;
  }

  public PersonId getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(PersonId endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }

  public abstract Integer getId();

  public abstract SectionClause getClause();

  public abstract Optional<SectionClause> getParentClause();

  public abstract void setClause(SectionClause clause);

  public abstract void setParentClause(SectionClause parentClause);

}
