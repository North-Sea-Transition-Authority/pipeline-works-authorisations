package uk.co.ogauthority.pwa.service.searchselector;

import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

/**
 * To be used for strings that are required to be searched.
 * This is similar to RestSearchItem, however implements SearchSelectable, and only stores a single value.
 *
 * <p>The intent is to separate searchable data from the returned results.</p>
 */
public class SearchSelection implements SearchSelectable {

  private final String value;

  public SearchSelection(String value) {
    this.value = value;
  }

  @Override
  public String getSelectionId() {
    return value;
  }

  @Override
  public String getSelectionText() {
    return value;
  }
}
