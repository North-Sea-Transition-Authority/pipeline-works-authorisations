package uk.co.ogauthority.pwa.service.searchselector;

import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

/**
 * To be used for strings that are required to be searched.
 * This is similar to RestSearchItem, however implements SearchSelectable, and only stores a single value.
 *
 * <p>The intent is to separate searchable data from the returned results.</p>
 */
public class SearchIdValueSelection implements SearchSelectable {

  private final String id;
  private final String value;

  public SearchIdValueSelection(String id, String value) {
    this.id = id;
    this.value = value;
  }

  @Override
  public String getSelectionId() {
    return id;
  }

  @Override
  public String getSelectionText() {
    return value;
  }
}
