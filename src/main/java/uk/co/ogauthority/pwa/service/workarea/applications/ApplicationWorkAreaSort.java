package uk.co.ogauthority.pwa.service.workarea.applications;

import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem_;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaSort;

public enum ApplicationWorkAreaSort implements WorkAreaSort {

  CREATED_DATE_DESC(ApplicationDetailSearchItem_.PAD_CREATED_TIMESTAMP, Sort.Direction.DESC),
  PROPOSED_START_DATE_ASC(ApplicationDetailSearchItem_.PAD_PROPOSED_START, Sort.Direction.ASC, Sort.NullHandling.NULLS_FIRST);

  private final String sortAttribute;
  private final Sort.Direction sortDirection;
  private final Sort.NullHandling nullHandling;

  ApplicationWorkAreaSort(String sortAttribute, Sort.Direction sortDirection, Sort.NullHandling nullHandling) {
    this.sortAttribute = sortAttribute;
    this.sortDirection = sortDirection;
    this.nullHandling = nullHandling;
    
  }

  ApplicationWorkAreaSort(String sortAttribute, Sort.Direction sortDirection) {
    this(sortAttribute, sortDirection, Sort.NullHandling.NULLS_LAST);
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
  public Sort.NullHandling getNullHandling() {
    return this.nullHandling;
  }


  @Override
  public String toString() {
    return "ApplicationWorkAreaSort{" +
        "sortAttribute='" + sortAttribute + '\'' +
        ", sortDirection=" + sortDirection +
        ", nullHandling=" + nullHandling +
        '}';
  }
}
