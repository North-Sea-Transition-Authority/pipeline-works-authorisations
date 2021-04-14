package uk.co.ogauthority.pwa.model.view.appprocessing.casehistory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Used to allow grouping of data items to allow easier manipulation of display.
 */
public class DataItemRow {

  private Map<String, String> dataItems;

  public DataItemRow() {
    this.dataItems = new LinkedHashMap<>();
  }

  public DataItemRow(Map<String, String> dataItems) {
    this.dataItems = dataItems;
  }

  public Map<String, String> getDataItems() {
    return dataItems;
  }

  public void setDataItems(Map<String, String> dataItems) {
    this.dataItems = dataItems;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataItemRow that = (DataItemRow) o;
    return Objects.equals(dataItems, that.dataItems);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataItems);
  }
}
