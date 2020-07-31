package uk.co.ogauthority.pwa.service.diff;

import uk.co.ogauthority.pwa.model.diff.DiffableAsString;

public class OtherDiffableAsStringClass implements DiffableAsString {

  String value;

  public OtherDiffableAsStringClass(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getDiffableString() {
    return value;
  }
}
