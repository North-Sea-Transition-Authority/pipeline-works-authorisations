package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.List;
import uk.co.ogauthority.pwa.model.searchselector.SearchResult;

public class PwaFieldForm {

  private Boolean linkedToField;

  private List<SearchResult> fieldIds;

  private String noLinkedFieldDescription;

  public Boolean getLinkedToField() {
    return linkedToField;
  }

  public void setLinkedToField(Boolean linkedToField) {
    this.linkedToField = linkedToField;
  }

  public List<SearchResult> getFieldIds() {
    return fieldIds;
  }

  public void setFieldIds(List<SearchResult> fieldIds) {
    this.fieldIds = fieldIds;
  }

  public String getNoLinkedFieldDescription() {
    return noLinkedFieldDescription;
  }

  public void setNoLinkedFieldDescription(String noLinkedFieldDescription) {
    this.noLinkedFieldDescription = noLinkedFieldDescription;
  }
}
