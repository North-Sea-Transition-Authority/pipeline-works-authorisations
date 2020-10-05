package uk.co.ogauthority.pwa.model.documents.view;

import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;

public class SectionClauseVersionView {

  private Integer id;

  private Integer clauseId;

  private String name;

  private String text;

  private Integer parentClauseId;

  private Integer levelNumber;

  private Integer levelOrder;

  private List<SectionClauseVersionView> childClauses;

  public SectionClauseVersionView(Integer id,
                                  Integer clauseId,
                                  String name,
                                  String text,
                                  Integer parentClauseId,
                                  Integer levelNumber,
                                  Integer levelOrder) {
    this.id = id;
    this.clauseId = clauseId;
    this.name = name;
    this.text = text;
    this.parentClauseId = parentClauseId;
    this.levelNumber = levelNumber;
    this.levelOrder = levelOrder;
    childClauses = new ArrayList<>();
  }

  public static SectionClauseVersionView from(DocumentInstanceSectionClauseVersionDto instanceSectionClauseVersionDto) {
    return new SectionClauseVersionView(
        instanceSectionClauseVersionDto.getDiscvId(),
        instanceSectionClauseVersionDto.getDiscId(),
        instanceSectionClauseVersionDto.getName(),
        instanceSectionClauseVersionDto.getText(),
        instanceSectionClauseVersionDto.getParentDiscId(),
        instanceSectionClauseVersionDto.getLevelNumber(),
        instanceSectionClauseVersionDto.getLevelOrder()
    );
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getClauseId() {
    return clauseId;
  }

  public void setClauseId(Integer clauseId) {
    this.clauseId = clauseId;
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

  public Integer getParentClauseId() {
    return parentClauseId;
  }

  public void setParentClauseId(Integer parentClauseId) {
    this.parentClauseId = parentClauseId;
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

  public List<SectionClauseVersionView> getChildClauses() {
    return childClauses;
  }

  public void setChildClauses(List<SectionClauseVersionView> childClauses) {
    this.childClauses = childClauses;
  }
}
