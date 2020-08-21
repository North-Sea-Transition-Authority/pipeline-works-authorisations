package uk.co.ogauthority.pwa.service.workarea;

import java.util.List;
import java.util.Objects;

/**
 * Captures basic details that can be displayed in any column of the workarea.
 */
public class WorkAreaColumnItemView {

  private final LabelType labelType;
  private final TagType valueTagType;
  private final String label;
  private final String value;
  private final List<String> listItems;

  private WorkAreaColumnItemView(LabelType labelType,
                                 TagType valueTagType,
                                 String label,
                                 String value,
                                 List<String> listItems) {
    this.labelType = labelType;
    this.valueTagType = valueTagType;
    this.label = label;
    this.value = value;
    this.listItems = listItems;
  }

  public static WorkAreaColumnItemView createLabelledItem(String label, String value) {
    return new WorkAreaColumnItemView(
        LabelType.DEFAULT,
        TagType.NONE,
        label,
        value,
        List.of());
  }

  public static WorkAreaColumnItemView createTagItem(TagType tagType, String value) {
    return new WorkAreaColumnItemView(
        LabelType.NONE,
        tagType,
        null,
        value,
        List.of());
  }

  public static WorkAreaColumnItemView createLinkItem(String label, String url) {
    return new WorkAreaColumnItemView(
        LabelType.LINK,
        TagType.NONE,
        label,
        url,
        List.of());
  }

  public LabelType getLabelType() {
    return labelType;
  }

  public TagType getValueTagType() {
    return valueTagType;
  }

  public String getLabel() {
    return label;
  }

  public String getValue() {
    return value;
  }

  public List<String> getListItems() {
    return listItems;
  }

  // Elements added here will need to be supported inside the workAreaItem view template
  public enum LabelType {
    NONE,
    DEFAULT,
    LINK
  }

  // Elements added here will need to be supported inside the workAreaItem view template
  public enum TagType {
    NONE,
    DEFAULT,
    INFO,
    SUCCESS,
    DANGER
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkAreaColumnItemView that = (WorkAreaColumnItemView) o;
    return labelType == that.labelType
        && valueTagType == that.valueTagType
        && Objects.equals(label, that.label)
        && Objects.equals(value, that.value)
        && Objects.equals(listItems, that.listItems);
  }

  @Override
  public int hashCode() {
    return Objects.hash(labelType, valueTagType, label, value, listItems);
  }
}
