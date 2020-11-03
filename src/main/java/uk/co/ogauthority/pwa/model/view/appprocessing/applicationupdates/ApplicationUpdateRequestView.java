package uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates;

import java.time.Instant;
import uk.co.ogauthority.pwa.util.DateUtils;

/**
 * Spring projection interface for
 * {@link uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest} entity.
 */
public interface ApplicationUpdateRequestView {

  String getRequestReason();

  Instant getRequestedTimestamp();

  default String getRequestedTimestampDisplay() {
    return DateUtils.formatDateTime(getRequestedTimestamp());
  }



}
