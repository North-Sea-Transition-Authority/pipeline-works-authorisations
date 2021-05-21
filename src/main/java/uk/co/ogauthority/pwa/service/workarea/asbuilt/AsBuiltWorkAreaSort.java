package uk.co.ogauthority.pwa.service.workarea.asbuilt;

import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaSort;

public enum AsBuiltWorkAreaSort implements WorkAreaSort {

  DEADLINE_DATE_ASC("deadlineDate", Sort.Direction.ASC);

  private final String sortAttribute;
  private final Sort.Direction sortDirection;
  private final Sort.NullHandling nullHandling;

  AsBuiltWorkAreaSort(String sortAttribute, Sort.Direction sortDirection, Sort.NullHandling nullHandling) {
    this.sortAttribute = sortAttribute;
    this.sortDirection = sortDirection;
    this.nullHandling = nullHandling;
  }

  AsBuiltWorkAreaSort(String sortAttribute, Sort.Direction sortDirection) {
    this(sortAttribute, sortDirection, Sort.NullHandling.NULLS_LAST);
  }

  @Override
  public String getSortAttribute() {
    return sortAttribute;
  }

  @Override
  public Sort.Direction getSortDirection() {
    return sortDirection;
  }

  @Override
  public Sort.NullHandling getNullHandling() {
    return this.nullHandling;
  }


  @Override
  public String toString() {
    return "AsBuiltWorkAreaSort{" +
        "sortAttribute='" + sortAttribute + '\'' +
        ", sortDirection=" + sortDirection +
        '}';
  }
}
