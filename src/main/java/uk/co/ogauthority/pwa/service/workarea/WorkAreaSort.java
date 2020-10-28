package uk.co.ogauthority.pwa.service.workarea;

import java.util.List;
import org.springframework.data.domain.Sort;

public interface WorkAreaSort {

  String getSortAttribute();

  Sort.Direction getSortDirection();

  Sort.NullHandling getNullHandling();

  default Sort getSort() {
    return Sort.by(
        List.of(new Sort.Order(getSortDirection(), getSortAttribute(), getNullHandling()))
    );
  }

}
