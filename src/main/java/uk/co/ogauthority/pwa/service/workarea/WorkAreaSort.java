package uk.co.ogauthority.pwa.service.workarea;

import java.util.List;
import org.springframework.data.domain.Sort;

public interface WorkAreaSort {

  String getPrimarySortAttribute();

  Sort.Direction getPrimarySortDirection();

  Sort.NullHandling getPrimaryNullHandling();

  default Sort getSort() {
    return Sort.by(
        List.of(new Sort.Order(getPrimarySortDirection(), getPrimarySortAttribute(), getPrimaryNullHandling()))
    );
  }

}
