package uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.util.DateUtils;

/**
 * Spring projection interface for
 * {@link uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest} entity.
 */
public interface ApplicationUpdateRequestView {

  @Value("#{target.pwaApplicationDetail.versionNo}")
  Integer getRequestedOnApplicationVersionNo();

  String getRequestReason();

  Instant getDeadlineTimestamp();

  default String getDeadlineTimestampDisplay() {
    return DateUtils.formatDate(getDeadlineTimestamp());
  }

  Instant getRequestedTimestamp();

  default String getRequestedTimestampDisplay() {
    return DateUtils.formatDateTime(getRequestedTimestamp());
  }

  PersonId getResponseByPersonId();

  Instant getResponseTimestamp();

  default String getResponseTimestampDisplay() {
    return DateUtils.formatDateTime(getResponseTimestamp());
  }

  String getResponseOtherChanges();


}
