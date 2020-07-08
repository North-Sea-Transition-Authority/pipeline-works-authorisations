package uk.co.ogauthority.pwa.service.controllers.typemismatch;

import java.math.BigDecimal;

public class TypeMismatchTestForm {

  private Integer integerField;
  private BigDecimal bigDecimalField;
  private String stringField;

  public TypeMismatchTestForm() {
  }

  public Integer getIntegerField() {
    return integerField;
  }

  public void setIntegerField(Integer integerField) {
    this.integerField = integerField;
  }

  public BigDecimal getBigDecimalField() {
    return bigDecimalField;
  }

  public void setBigDecimalField(BigDecimal bigDecimalField) {
    this.bigDecimalField = bigDecimalField;
  }

  public String getStringField() {
    return stringField;
  }

  public void setStringField(String stringField) {
    this.stringField = stringField;
  }
}
