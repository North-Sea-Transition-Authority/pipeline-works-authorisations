package uk.co.ogauthority.pwa.service.diff;

import uk.co.ogauthority.pwa.model.view.StringWithTag;

public class SimpleDiffTestClass {

  private String stringField;
  private Integer integerField;
  private StringWithTag stringWithTagField;

  public SimpleDiffTestClass(
      String stringField,
      Integer integerField,
      StringWithTag stringWithTagField
  ) {
    this.stringField = stringField;
    this.integerField = integerField;
    this.stringWithTagField = stringWithTagField;
  }

  public String getStringField() {
    return stringField;
  }

  public void setStringField(String stringField) {
    this.stringField = stringField;
  }

  public Integer getIntegerField() {
    return integerField;
  }

  public void setIntegerField(Integer integerField) {
    this.integerField = integerField;
  }

  public StringWithTag getStringWithTagField() {
    return stringWithTagField;
  }

  public void setStringWithTagField(StringWithTag stringWithTagField) {
    this.stringWithTagField = stringWithTagField;
  }
}
