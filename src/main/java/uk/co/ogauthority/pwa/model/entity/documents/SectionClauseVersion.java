package uk.co.ogauthority.pwa.model.entity.documents;

import java.time.Instant;
import java.util.Optional;
import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
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

}
