package uk.co.ogauthority.pwa.model.documents.instances;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.documents.SectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

@Entity
@Immutable
@Table(name = "vw_di_clause_versions")
public class DocumentInstanceSectionClauseVersionDto implements SectionClauseVersionDto {

  @Id
  @Column(name = "discv_id")
  private Integer discvId;

  // not mapped for performance benefit, not needed beyond querying
  @Column(name = "di_id")
  private Integer diId;

  private String sectionName;

  @Column(name = "di_sc_id")
  private Integer discId;

  private Integer versionNo;

  private Boolean tipFlag;

  private String name;

  private String text;

  @Column(name = "parent_di_sc_id")
  private Integer parentDiscId;

  private Integer levelNumber;

  private Integer levelOrder;

  @Enumerated(EnumType.STRING)
  private SectionClauseVersionStatus status;

  private Instant createdTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "created_by_person_id")
  private PersonId createdByPersonId;

  private Instant endedTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "ended_by_person_id")
  private PersonId endedByPersonId;

  public DocumentInstanceSectionClauseVersionDto() {
  }

  public Integer getDiscvId() {
    return discvId;
  }

  @Override
  public Integer getVersionId() {
    return getDiscvId();
  }

  public void setDiscvId(Integer discvId) {
    this.discvId = discvId;
  }

  public void setDiId(Integer diId) {
    this.diId = diId;
  }

  @Override
  public String getSectionName() {
    return sectionName;
  }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }

  @Override
  public Integer getClauseId() {
    return discId;
  }

  public void setClauseId(Integer discId) {
    this.discId = discId;
  }

  @Override
  public Integer getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(Integer versionNo) {
    this.versionNo = versionNo;
  }

  @Override
  public Boolean getTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(Boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public Integer getParentClauseId() {
    return parentDiscId;
  }

  public void setParentClauseId(Integer parentDiscId) {
    this.parentDiscId = parentDiscId;
  }

  @Override
  public Integer getLevelNumber() {
    return levelNumber;
  }

  public void setLevelNumber(Integer levelNumber) {
    this.levelNumber = levelNumber;
  }

  @Override
  public Integer getLevelOrder() {
    return levelOrder;
  }

  public void setLevelOrder(Integer levelOrder) {
    this.levelOrder = levelOrder;
  }

  @Override
  public SectionClauseVersionStatus getStatus() {
    return status;
  }

  public void setStatus(SectionClauseVersionStatus status) {
    this.status = status;
  }

  @Override
  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  @Override
  public PersonId getCreatedByPersonId() {
    return createdByPersonId;
  }

  public void setCreatedByPersonId(PersonId createdByPersonId) {
    this.createdByPersonId = createdByPersonId;
  }

  @Override
  public Instant getEndedTimestamp() {
    return endedTimestamp;
  }

  public void setEndedTimestamp(Instant endedTimestamp) {
    this.endedTimestamp = endedTimestamp;
  }

  @Override
  public PersonId getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(PersonId endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }

}
