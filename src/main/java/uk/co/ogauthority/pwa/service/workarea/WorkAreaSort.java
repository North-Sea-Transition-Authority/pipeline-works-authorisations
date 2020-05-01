package uk.co.ogauthority.pwa.service.workarea;

import org.springframework.data.domain.Sort;

public enum WorkAreaSort {
  CREATED_DATE_DESC("padCreatedTimestamp", Sort.Direction.DESC),
  PROPOSED_DATE_ASC("padProposedStart", Sort.Direction.ASC);

  private final String sortAttribute;
  private final Sort.Direction sortDirection;

  WorkAreaSort(String sortAttribute, Sort.Direction sortDirection) {
    this.sortAttribute = sortAttribute;
    this.sortDirection = sortDirection;
  }

  public Sort getSort() {
    return Sort.by(this.sortDirection, sortAttribute);
  }

  String getSortAttribute() {
    return this.sortAttribute;
  }

  @Override
  public String toString() {
    return "WorkAreaSort{" +
        "name='" + this.name() +
        ", sortAttribute='" + sortAttribute + '\'' +
        ", sortDirection=" + sortDirection +
        '}';
  }
}
