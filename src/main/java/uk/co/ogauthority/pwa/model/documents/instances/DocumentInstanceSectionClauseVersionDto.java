package uk.co.ogauthority.pwa.model.documents.instances;

import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;

@Entity
@Immutable
@Table(name = "vw_di_clause_versions")
public class DocumentInstanceSectionClauseVersionDto {

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

  private String status;

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

  public void setDiscvId(Integer discvId) {
    this.discvId = discvId;
  }

  public Integer getDiId() {
    return diId;
  }

  public void setDiId(Integer diId) {
    this.diId = diId;
  }

  public String getSectionName() {
    return sectionName;
  }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }

  public Integer getDiscId() {
    return discId;
  }

  public void setDiscId(Integer discId) {
    this.discId = discId;
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

  public Integer getParentDiscId() {
    return parentDiscId;
  }

  public void setParentDiscId(Integer parentDiscId) {
    this.parentDiscId = parentDiscId;
  }

  public Integer getLevelNumber() {
    return levelNumber;
  }

  public void setLevelNumber(Integer levelNumber) {
    this.levelNumber = levelNumber;
  }

  public Integer getLevelOrder() {
    return levelOrder;
  }

  public void setLevelOrder(Integer levelOrder) {
    this.levelOrder = levelOrder;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
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
}
