package uk.co.ogauthority.pwa.model.view;

public class StringWithTag {

  private String value;

  private Tag tag;

  public StringWithTag() {
    this("", Tag.NONE);
  }

  public StringWithTag(String value) {
    this(value, Tag.NONE);
  }

  public StringWithTag(String value, Tag tag) {
    this.value = value;
    this.tag = tag;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Tag getTag() {
    return tag;
  }

  public void setTag(Tag tag) {
    this.tag = tag;
  }
}
