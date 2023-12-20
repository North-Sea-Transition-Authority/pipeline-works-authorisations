package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.StringWithTagItem;

/**
 * General purpose view to display information from application or master pwa about links to DEVUK fields.
 */
public class PwaAreaLinksView {

  private final Boolean isLinkedToAreas;
  private final String pwaLinkedToDescription;

  private final List<StringWithTagItem> linkedAreaNames;

  public PwaAreaLinksView(Boolean isLinkedToAreas,
                          String pwaLinkedToDescription,
                          List<StringWithTag> linkedAreaNames) {
    this.isLinkedToAreas = isLinkedToAreas;
    this.pwaLinkedToDescription = pwaLinkedToDescription;
    // only set field names if flag set even if field names provided
    this.linkedAreaNames = BooleanUtils.isTrue(isLinkedToAreas)
        ? linkedAreaNames.stream()
        .sorted(Comparator.comparing(StringWithTag::getValue))
        .map(StringWithTagItem::new)
        .collect(Collectors.toList())
        : List.of();
  }

  public Boolean getLinkedToFields() {
    return isLinkedToAreas;
  }

  public String getPwaLinkedToDescription() {
    return pwaLinkedToDescription;
  }

  public List<StringWithTagItem> getLinkedAreaNames() {
    return linkedAreaNames;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaAreaLinksView that = (PwaAreaLinksView) o;
    return Objects.equals(isLinkedToAreas, that.isLinkedToAreas) && Objects.equals(
        pwaLinkedToDescription, that.pwaLinkedToDescription) && Objects.equals(linkedAreaNames,
        that.linkedAreaNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isLinkedToAreas, pwaLinkedToDescription, linkedAreaNames);
  }

}
