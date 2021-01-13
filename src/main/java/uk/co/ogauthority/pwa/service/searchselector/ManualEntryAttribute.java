package uk.co.ogauthority.pwa.service.searchselector;

/**
 * <p>Modifier to be used with SearchSelectorService::addManualEntry
 * Allows free text prefix to be excluded from the result ID.</p>
 *
 * <p>The use case for this enum is to allow text to be searched over when there is no corresponding ID.
 * SearchSelectorService::addManualEntry by default will apply {WITH_FREE_TEXT_PREFIX}.
 * This will result in the following being stored in the database: "FT_Text".</p>
 *
 * <p>The same call to SearchSelectorService::addManualEntry, but with {NO_FREE_TEXT_PREFIX} applied
 * will result in the following being stored in the database: "Text".</p>
 *
 * <p>This is primarily helpful in terms of displaying data, as there's no need to strip the prefix
 * prior to displaying the search results.</p>
 */
public enum ManualEntryAttribute {

  WITH_FREE_TEXT_PREFIX,
  NO_FREE_TEXT_PREFIX;

}
