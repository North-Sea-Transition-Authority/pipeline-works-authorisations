package uk.co.ogauthority.pwa.model.documents.templates;

import java.time.Instant;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

public class TemplateSectionClauseVersionDto {

  private Integer id;

  private DocumentTemplateSectionClause templateClauseRecord;

  private Integer versionNo;

  private Boolean tipFlag;

  private String name;

  private String text;

  private DocumentTemplateSectionClause parentTemplateClause;

  private Integer levelOrder;

  private SectionClauseVersionStatus status;

  private Instant createdTimestamp;

  private PersonId createdByPersonId;

  private Instant endedTimestamp;

  private PersonId endedByPersonId;

  public TemplateSectionClauseVersionDto() {
  }

  public static TemplateSectionClauseVersionDto from(DocumentTemplateSectionClauseVersion version) {

    var dto = new TemplateSectionClauseVersionDto();

    dto.setId(version.getId());
    dto.setTemplateClauseRecord(version.getClause());
    dto.setVersionNo(version.getVersionNo());
    dto.setTipFlag(version.getTipFlag());
    dto.setName(version.getName());
    dto.setText(version.getText());
    dto.setParentTemplateClause(((DocumentTemplateSectionClause) version.getParentClause().orElse(null)));
    dto.setLevelOrder(version.getLevelOrder());
    dto.setStatus(version.getStatus());
    dto.setCreatedByPersonId(version.getCreatedByPersonId());
    dto.setCreatedTimestamp(version.getCreatedTimestamp());
    dto.setEndedByPersonId(version.getEndedByPersonId());
    dto.setEndedTimestamp(version.getEndedTimestamp());

    return dto;

  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DocumentTemplateSectionClause getTemplateClauseRecord() {
    return templateClauseRecord;
  }

  public void setTemplateClauseRecord(
      DocumentTemplateSectionClause templateClauseRecord) {
    this.templateClauseRecord = templateClauseRecord;
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

  public DocumentTemplateSectionClause getParentTemplateClause() {
    return parentTemplateClause;
  }

  public void setParentTemplateClause(
      DocumentTemplateSectionClause parentTemplateClause) {
    this.parentTemplateClause = parentTemplateClause;
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
}
