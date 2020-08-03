package uk.co.ogauthority.pwa.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaSort;

public class WorkAreaUtils {

  private WorkAreaUtils() {
    throw new AssertionError();
  }

  public static Pageable getWorkAreaPageRequest(int pageRequest, WorkAreaSort workAreaSort) {
    return PageRequest.of(pageRequest, WorkAreaService.PAGE_SIZE, workAreaSort.getSort());
  }

}
