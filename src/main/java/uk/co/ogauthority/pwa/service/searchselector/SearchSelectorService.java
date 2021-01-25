package uk.co.ogauthority.pwa.service.searchselector;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

/**
 * A generic service to provide a list of RestSearchItems for any entities implementing SearchSelectable.
 * An optional {@link #addManualEntry} method is provided if the endpoint requires manually entered text.
 */
@Service
public class SearchSelectorService {

  public List<RestSearchItem> search(String searchQuery, Collection<? extends SearchSelectable> selectableList) {
    return selectableList.stream()
        .filter(searchSelectable ->
            searchSelectable.getSelectionText()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(searchQuery, "").toLowerCase()))
        .map(item -> new RestSearchItem(item.getSelectionId(), item.getSelectionText()))
        .collect(Collectors.toList());
  }

  public List<RestSearchItem> addManualEntry(String searchQuery, List<RestSearchItem> resultList) {
    return addManualEntry(searchQuery, resultList, ManualEntryAttribute.WITH_FREE_TEXT_PREFIX);
  }

  public List<RestSearchItem> addManualEntry(String searchQuery, List<RestSearchItem> resultList,
                                             ManualEntryAttribute manualEntryAttribute) {
    if (!StringUtils.isBlank(searchQuery)) {
      var entryExists = resultList.stream()
          .anyMatch(restSearchItem -> restSearchItem.getText().toLowerCase().equals(searchQuery.toLowerCase()));
      if (!entryExists) {
        if (manualEntryAttribute.equals(ManualEntryAttribute.WITH_FREE_TEXT_PREFIX)) {
          resultList.add(0, new RestSearchItem(SearchSelectable.FREE_TEXT_PREFIX + searchQuery, searchQuery));
        } else {
          resultList.add(0, new RestSearchItem(searchQuery, searchQuery));
        }
      }
    }
    return resultList;
  }

  public static String route(Object methodCall) {
    return StringUtils.stripEnd(ReverseRouter.route(methodCall), "?term");
  }

  /**
   * Build a map of manual entries and linked entries, with the linked entry display text.
   *
   * @param selections             All selected items from a form field.
   * @param resolvedLinkedEntryMap A map of ID (String) -> DisplayText (String).
   * @return A map of selection results to pre-populate the search selector.
   */
  public Map<String, String> buildPrepopulatedSelections(List<String> selections,
                                                         Map<String, String> resolvedLinkedEntryMap) {
    var results = new LinkedHashMap<String, String>();
    for (String s : selections) {
      if (s.startsWith(SearchSelectable.FREE_TEXT_PREFIX)) {
        results.put(s, removePrefix(s));
      } else {
        results.put(s, resolvedLinkedEntryMap.get(s));
      }
    }
    return results;
  }

  public String removePrefix(String s) {
    return StringUtils.substring(s, SearchSelectable.FREE_TEXT_PREFIX.length());
  }

}
