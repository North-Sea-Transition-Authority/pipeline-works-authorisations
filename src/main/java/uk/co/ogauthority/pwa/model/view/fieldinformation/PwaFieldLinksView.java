package uk.co.ogauthority.pwa.model.view.fieldinformation;

import java.util.Comparator;
import java.util.List;
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
}
