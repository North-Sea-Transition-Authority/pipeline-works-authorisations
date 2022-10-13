package uk.co.ogauthority.pwa.model.searchselector;

import java.util.Objects;

/**
 * Wrapper to encapsulate search result strings to prevent Spring splitting
 * results which contain commas into multiple results.
 */
public class SearchResult {

  private final String value;

  public SearchResult(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    SearchResult searchResult = (SearchResult) object;
    return Objects.equals(value, searchResult.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
