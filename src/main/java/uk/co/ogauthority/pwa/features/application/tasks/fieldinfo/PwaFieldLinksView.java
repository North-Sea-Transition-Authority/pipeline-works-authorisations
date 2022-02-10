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
public class PwaFieldLinksView {

  private final Boolean isLinkedToFields;
  private final String pwaLinkedToDescription;

  private final List<StringWithTagItem> linkedFieldNames;

  public PwaFieldLinksView(Boolean isLinkedToFields,
                           String pwaLinkedToDescription,
                           List<StringWithTag> linkedFieldNames) {
    this.isLinkedToFields = isLinkedToFields;
    this.pwaLinkedToDescription = pwaLinkedToDescription;
    // only set field names if flag set even if field names provided
    this.linkedFieldNames = BooleanUtils.isTrue(isLinkedToFields)
        ? linkedFieldNames.stream()
        .sorted(Comparator.comparing(StringWithTag::getValue))
        .map(StringWithTagItem::new)
        .collect(Collectors.toList())
        : List.of();
  }

  public Boolean getLinkedToFields() {
    return isLinkedToFields;
  }

  public String getPwaLinkedToDescription() {
    return pwaLinkedToDescription;
  }

  public List<StringWithTagItem> getLinkedFieldNames() {
    return linkedFieldNames;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaFieldLinksView that = (PwaFieldLinksView) o;
    return Objects.equals(isLinkedToFields, that.isLinkedToFields) && Objects.equals(
        pwaLinkedToDescription, that.pwaLinkedToDescription) && Objects.equals(linkedFieldNames,
        that.linkedFieldNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isLinkedToFields, pwaLinkedToDescription, linkedFieldNames);
  }

}
