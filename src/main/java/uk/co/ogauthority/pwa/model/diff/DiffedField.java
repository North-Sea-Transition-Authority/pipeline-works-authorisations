package uk.co.ogauthority.pwa.model.diff;

import uk.co.ogauthority.pwa.model.view.Tag;

public class DiffedField {

  private DiffType diffType;

  private String currentValue;

  private Tag currentValueTag;

  private String previousValue;

  private Tag previousValueTag;

  public DiffedField(DiffType diffType, String currentValue, String previousValue, Tag currentValueTag, Tag previousValueTag) {
    this.diffType = diffType;
    this.currentValue = currentValue;
    this.previousValue = previousValue;
    this.currentValueTag = currentValueTag != null ? currentValueTag : Tag.NONE;
    this.previousValueTag = previousValueTag != null ? previousValueTag : Tag.NONE;
  }

  public DiffedField(DiffType diffType, String currentValue, String previousValue) {
    this(diffType, currentValue, previousValue, Tag.NONE, Tag.NONE);
  }

  public DiffType getDiffType() {
    return diffType;
  }

  public String getCurrentValue() {
    return currentValue;
  }

  public String getPreviousValue() {
    return previousValue;
  }

  public Tag getCurrentValueTag() {
    return currentValueTag;
  }

  public void setCurrentValueTag(Tag currentValueTag) {
    this.currentValueTag = currentValueTag;
  }

  public Tag getPreviousValueTag() {
    return previousValueTag;
  }

  public void setPreviousValueTag(Tag previousValueTag) {
    this.previousValueTag = previousValueTag;
  }
}
