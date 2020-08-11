package uk.co.ogauthority.pwa.service.diff;

import uk.co.ogauthority.pwa.model.diff.DiffableAsString;
import uk.co.ogauthority.pwa.model.view.StringWithTag;

public class SimpleDiffTestClass {

  private Boolean booleanField;
  private String stringField;
  private Integer integerField;
  private StringWithTag stringWithTagField;
  private DiffableAsString diffableAsString;

  public SimpleDiffTestClass(
      Boolean booleanField,
      String stringField,
      Integer integerField,
      StringWithTag stringWithTagField,
      DiffableAsString diffableAsString) {
    this.booleanField = booleanField;
    this.stringField = stringField;
    this.integerField = integerField;
    this.stringWithTagField = stringWithTagField;
    this.diffableAsString = diffableAsString;
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

  public DiffableAsString getDiffableAsString() {
    return diffableAsString;
  }

  public void setDiffableAsString(DiffableAsString diffableAsString) {
    this.diffableAsString = diffableAsString;
  }

  public Boolean getBooleanField() {
    return booleanField;
  }

  public void setBooleanField(Boolean booleanField) {
    this.booleanField = booleanField;
  }
}
