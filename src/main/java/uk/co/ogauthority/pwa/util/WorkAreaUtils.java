package uk.co.ogauthority.pwa.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaSort;

public class WorkAreaUtils {

  public static final DateTimeFormatter WORK_AREA_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      .withZone(ZoneId.systemDefault());
  public static final DateTimeFormatter WORK_AREA_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
      .withZone(ZoneId.systemDefault());

  private WorkAreaUtils() {
    throw new AssertionError();
  }

  public static Pageable getWorkAreaPageRequest(int pageRequest, WorkAreaSort workAreaSort) {
    return PageRequest.of(pageRequest, WorkAreaService.PAGE_SIZE, workAreaSort.getSort());
  }

}
