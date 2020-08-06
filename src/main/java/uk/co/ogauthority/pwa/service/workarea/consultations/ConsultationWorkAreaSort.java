package uk.co.ogauthority.pwa.service.workarea.consultations;

import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaSort;

public enum ConsultationWorkAreaSort implements WorkAreaSort {

  DEADLINE_DATE_DESC("deadlineDate", Sort.Direction.DESC);

  private final String sortAttribute;
  private final Sort.Direction sortDirection;

  ConsultationWorkAreaSort(String sortAttribute, Sort.Direction sortDirection) {
    this.sortAttribute = sortAttribute;
    this.sortDirection = sortDirection;
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
  public String toString() {
    return "ConsultationWorkAreaSort{" +
        "sortAttribute='" + sortAttribute + '\'' +
        ", sortDirection=" + sortDirection +
        '}';
  }
}
