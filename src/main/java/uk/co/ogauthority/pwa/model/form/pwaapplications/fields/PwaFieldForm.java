package uk.co.ogauthority.pwa.model.form.pwaapplications.fields;

import javax.validation.constraints.NotNull;

public class PwaFieldForm {

  @NotNull(message = "You must select a field")
  private Boolean linkedToField;

  private Integer fieldId;

  private String noLinkedFieldDescription;

  public Boolean getLinkedToField() {
    return linkedToField;
  }

  public void setLinkedToField(Boolean linkedToField) {
    this.linkedToField = linkedToField;
  }

  public Integer getFieldId() {
    return fieldId;
  }

  public void setFieldId(Integer fieldId) {
    this.fieldId = fieldId;
  }

  public String getNoLinkedFieldDescription() {
    return noLinkedFieldDescription;
  }

  public void setNoLinkedFieldDescription(String noLinkedFieldDescription) {
    this.noLinkedFieldDescription = noLinkedFieldDescription;
  }
}
