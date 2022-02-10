package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.List;

public class PwaFieldForm {

  private Boolean linkedToField;

  private List<String> fieldIds;

  private String noLinkedFieldDescription;

  public Boolean getLinkedToField() {
    return linkedToField;
  }

  public void setLinkedToField(Boolean linkedToField) {
    this.linkedToField = linkedToField;
  }

  public List<String> getFieldIds() {
    return fieldIds;
  }

  public void setFieldIds(List<String> fieldIds) {
    this.fieldIds = fieldIds;
  }

  public String getNoLinkedFieldDescription() {
    return noLinkedFieldDescription;
  }

  public void setNoLinkedFieldDescription(String noLinkedFieldDescription) {
    this.noLinkedFieldDescription = noLinkedFieldDescription;
  }
}
