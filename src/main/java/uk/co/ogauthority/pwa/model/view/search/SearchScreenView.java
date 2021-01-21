package uk.co.ogauthority.pwa.model.view.search;

import java.util.List;
import java.util.Objects;

public class SearchScreenView<T> {

  private final long fullResultCount;
  private final List<T> searchResults;

  public SearchScreenView(long fullResultCount, List<T> searchResults) {
    this.fullResultCount = fullResultCount;
    this.searchResults = searchResults;
  }

  public long getFullResultCount() {
    return fullResultCount;
  }

  public List<T> getSearchResults() {
    return searchResults;
  }

  public boolean resultsHaveBeenLimited() {
    return fullResultCount > searchResults.size();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchScreenView<?> that = (SearchScreenView<?>) o;
    return fullResultCount == that.fullResultCount && Objects.equals(searchResults, that.searchResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fullResultCount, searchResults);
  }
}
