package uk.co.ogauthority.pwa.service.workarea.applications;

import java.util.List;
import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem_;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaSort;

public enum ApplicationWorkAreaSort implements WorkAreaSort {

  CREATED_DATE_DESC(WorkAreaApplicationDetailSearchItem_.PAD_CREATED_TIMESTAMP, Sort.Direction.DESC),
  PROPOSED_START_DATE_ASC(WorkAreaApplicationDetailSearchItem_.PAD_PROPOSED_START, Sort.Direction.ASC, Sort.NullHandling.NULLS_FIRST),
  SUBMITTED_APP_START_ASC_THEN_DRAFT_APP_START_ASC(
      WorkAreaApplicationDetailSearchItem_.SUBMITTED_FLAG, Sort.Direction.DESC, Sort.NullHandling.NULLS_LAST) {
    @Override
    public Sort getSort() {
      return Sort.by(
          List.of(
              new Sort.Order(getPrimarySortDirection(), getPrimarySortAttribute(), getPrimaryNullHandling()),
              new Sort.Order(Sort.Direction.ASC, WorkAreaApplicationDetailSearchItem_.PAD_PROPOSED_START, Sort.NullHandling.NULLS_LAST)
          )
      );
    }
  };

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
  public String getPrimarySortAttribute() {
    return this.sortAttribute;
  }

  @Override
  public Sort.Direction getPrimarySortDirection() {
    return this.sortDirection;
  }

  @Override
  public Sort.NullHandling getPrimaryNullHandling() {
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
