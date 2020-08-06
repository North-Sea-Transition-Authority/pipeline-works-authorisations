package uk.co.ogauthority.pwa.service.workarea.applications;

import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaSort;

public enum ApplicationWorkAreaSort implements WorkAreaSort {

  CREATED_DATE_DESC("padCreatedTimestamp", Sort.Direction.DESC),
  PROPOSED_DATE_ASC("padProposedStart", Sort.Direction.ASC);

  private final String sortAttribute;
  private final Sort.Direction sortDirection;

  ApplicationWorkAreaSort(String sortAttribute, Sort.Direction sortDirection) {
    this.sortAttribute = sortAttribute;
    this.sortDirection = sortDirection;
  }

  @Override
  public String getSortAttribute() {
    return this.sortAttribute;
  }

  @Override
  public Sort.Direction getSortDirection() {
    return this.sortDirection;
  }

  @Override
  public String toString() {
    return "ApplicationWorkAreaSort{" +
        "sortAttribute='" + sortAttribute + '\'' +
        ", sortDirection=" + sortDirection +
        '}';
  }
}
