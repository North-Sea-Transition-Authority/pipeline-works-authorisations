package uk.co.ogauthority.pwa.model.documents;

import java.util.List;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;

public class SectionDto {

  private String name;

  private List<TemplateSectionClauseVersionDto> clauses;

  public SectionDto() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<TemplateSectionClauseVersionDto> getClauses() {
    return clauses;
  }

  public void setClauses(List<TemplateSectionClauseVersionDto> clauses) {
    this.clauses = clauses;
  }
}
