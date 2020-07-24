package uk.co.ogauthority.pwa.model.form.pwaapplications.fields;

public class PwaFieldForm {

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
