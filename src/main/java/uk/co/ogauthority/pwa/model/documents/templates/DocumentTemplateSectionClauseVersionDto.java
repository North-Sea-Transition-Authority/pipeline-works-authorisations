package uk.co.ogauthority.pwa.model.documents.templates;

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
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

@Entity
@Immutable
@Table(name = "vw_dt_clause_versions")
public class DocumentTemplateSectionClauseVersionDto implements SectionClauseVersionDto {

  @Id
  @Column(name = "scv_id")
  private Integer scvId;

  private Integer dtId;

  @Column(name = "dt_mnem")
  @Enumerated(EnumType.STRING)
  private DocumentTemplateMnem documentTemplateMnem;

  @Column(name = "section_name")
  @Enumerated(EnumType.STRING)
  private DocumentSection section;

  @Column(name = "sc_id")
  private Integer scId;

  private Integer versionNo;

  private Boolean tipFlag;

  private String name;

  private String text;

  @Column(name = "parent_sc_id")
  private Integer parentScId;

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

  public DocumentTemplateSectionClauseVersionDto() {
  }

  public Integer getScvId() {
    return scvId;
  }

  @Override
  public Integer getVersionId() {
    return getScvId();
  }

  public void setScvId(Integer scvId) {
    this.scvId = scvId;
  }

  public Integer getDtId() {
    return dtId;
  }

  public void setDtId(Integer dtId) {
    this.dtId = dtId;
  }

  public DocumentTemplateMnem getDocumentTemplateMnem() {
    return documentTemplateMnem;
  }

  public void setDocumentTemplateMnem(
      DocumentTemplateMnem documentTemplateMnem) {
    this.documentTemplateMnem = documentTemplateMnem;
  }

  public DocumentSection getSection() {
    return section;
  }

  public void setSection(DocumentSection section) {
    this.section = section;
  }

  @Override
  public String getSectionName() {
    return section.name();
  }

  @Override
  public Integer getClauseId() {
    return scId;
  }

  public void setClauseId(Integer scId) {
    this.scId = scId;
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
    return parentScId;
  }

  public void setParentClauseId(Integer parentScId) {
    this.parentScId = parentScId;
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
