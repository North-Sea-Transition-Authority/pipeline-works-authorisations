package uk.co.ogauthority.pwa.model.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.collections4.ListUtils;

public class SearchSelectionView<T> {

  private List<String> manualEntries;
  private List<T> linkedEntries;

  public SearchSelectionView(List<String> entries, Function<String, T> builder) {
    manualEntries = new ArrayList<>();
    linkedEntries = new ArrayList<>();

    for (String s : entries) {
      if (s.startsWith(SearchSelectable.FREE_TEXT_PREFIX)) {
        manualEntries.add(s);
      } else {
        linkedEntries.add(builder.apply(s));
      }
    }
  }

  public List<String> getManualEntries() {
    return Collections.unmodifiableList(manualEntries);
  }

  public List<T> getLinkedEntries() {
    return ListUtils.unmodifiableList(linkedEntries);
  }
}
