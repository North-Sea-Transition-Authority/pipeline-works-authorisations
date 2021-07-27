package uk.co.ogauthority.pwa.model.enums.aabuilt;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;

public enum AsBuiltNotificationStatus {
  MIGRATION("Migrated", List.of(), StatusCategory.INACTIVE),

  PER_CONSENT("Exactly as per consent", List.of()),
  NOT_PER_CONSENT("Not exactly as per consent", List.of()),
  NEVER_LAID("Not laid and will not be laid", List.of(PipelineStatus.IN_SERVICE, PipelineStatus.RETURNED_TO_SHORE)),
  NOT_COMPLETED_IN_CONSENT_TIMEFRAME("Work not completed in the consent timeframe but will be completed at a later date", List.of()),
  NOT_PROVIDED("Not provided", List.of());

  private final String displayName;
  private final List<PipelineStatus> nonSupportedPipelineStatuses;
  private final StatusCategory statusCategory;

  AsBuiltNotificationStatus(String displayName,
                            List<PipelineStatus> nonSupportedPipelineStatuses,
                            StatusCategory statusCategory) {
    this.displayName = displayName;
    this.nonSupportedPipelineStatuses = nonSupportedPipelineStatuses;
    this.statusCategory = statusCategory;
  }

  AsBuiltNotificationStatus(String displayName,
                            List<PipelineStatus> nonSupportedPipelineStatuses) {
    this(displayName, nonSupportedPipelineStatuses, StatusCategory.ACTIVE);
  }

  public String getDisplayName() {
    return displayName;
  }

  public List<PipelineStatus> getNonSupportedPipelineStatuses() {
    return nonSupportedPipelineStatuses;
  }

  public StatusCategory getStatusCategory() {
    return statusCategory;
  }

  private static Stream<AsBuiltNotificationStatus> getActiveStatusesStream() {
    return Arrays.stream(AsBuiltNotificationStatus.values())
        .filter(asBuiltNotificationStatus -> StatusCategory.ACTIVE == asBuiltNotificationStatus.statusCategory);
  }

  public static Set<AsBuiltNotificationStatus> getActiveStatusSet() {
    return getActiveStatusesStream()
        .collect(Collectors.toUnmodifiableSet());
  }

  public static List<AsBuiltNotificationStatus> asList(PipelineStatus pipelineStatus) {
    return getActiveStatusesStream()
        .filter(asBuiltNotificationStatus -> !asBuiltNotificationStatus.nonSupportedPipelineStatuses.contains(pipelineStatus))
        .collect(toList());
  }

  enum StatusCategory {
    ACTIVE, INACTIVE
  }

}