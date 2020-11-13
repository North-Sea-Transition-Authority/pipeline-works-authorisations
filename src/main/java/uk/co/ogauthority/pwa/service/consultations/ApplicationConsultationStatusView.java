package uk.co.ogauthority.pwa.service.consultations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

/**
 * Object capturing counts of consulation requests with each status.
 */
public class ApplicationConsultationStatusView {

  private final Map<ConsultationRequestStatus, Long> statusCounts;

  ApplicationConsultationStatusView(Map<ConsultationRequestStatus, Long> sourceStatusCounts) {

    this.statusCounts = new HashMap<>();

    // fill in any missing status in the map.
    for (ConsultationRequestStatus status : ConsultationRequestStatus.values()) {
      this.statusCounts.put(
          status,
          sourceStatusCounts.getOrDefault(status, 0L)
      );
    }

  }

  public long getCountOfRequestsWithStatus(ConsultationRequestStatus consultationRequestStatus) {
    return this.statusCounts.getOrDefault(consultationRequestStatus, 0L);
  }


  public long sumFilteredStatusCounts(Function<ConsultationRequestStatus, Boolean> statusFilter) {
    return this.statusCounts.entrySet()
        .stream()
        .filter(consultationRequestStatusLongEntry -> statusFilter.apply(consultationRequestStatusLongEntry.getKey()))
        .mapToLong(o -> ObjectUtils.defaultIfNull(o.getValue(), 0L))
        .sum();
  }


  public Map<ConsultationRequestStatus, Long> getStatusCounts() {
    return Collections.unmodifiableMap(statusCounts);
  }
}
