package uk.co.ogauthority.pwa.model.view;

import java.util.Objects;

/**
 * Wrapper class for a String with tag allowing DiffService to operate on the whole object when put in a list, and not
 * on just the individual fields.
 */
public final class StringWithTagItem {

  private final StringWithTag stringWithTag;

  public StringWithTagItem(StringWithTag stringWithTag) {
    this.stringWithTag = stringWithTag;
  }

  public StringWithTag getStringWithTag() {
    return stringWithTag;
  }

  public int getStringWithTagHashcode() {
    return this.stringWithTag.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StringWithTagItem that = (StringWithTagItem) o;
    return Objects.equals(stringWithTag, that.stringWithTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stringWithTag);
  }
}