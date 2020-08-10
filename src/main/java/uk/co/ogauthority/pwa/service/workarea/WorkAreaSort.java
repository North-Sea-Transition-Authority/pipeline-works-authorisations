package uk.co.ogauthority.pwa.service.workarea;

import org.springframework.data.domain.Sort;

public interface WorkAreaSort {

  String getSortAttribute();

  Sort.Direction getSortDirection();

  default Sort getSort() {
    return Sort.by(getSortDirection(), getSortAttribute());
  }

}
