package uk.co.ogauthority.pwa.model.searchselector;

/**
 * SearchSelectable provides the required information required to produce a RestSearchItem.
 * A list of SearchSelectable implementations are used by the SearchSelectorService.
 */
public interface SearchSelectable {

  String FREE_TEXT_PREFIX = "FT_";

  String getSelectionId();

  String getSelectionText();

}
