package uk.co.ogauthority.pwa.model.enums.aabuilt;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;

public enum AsBuiltNotificationStatus {
  MIGRATION("Migrated", List.of(), false),

  PER_CONSENT("Exactly as per consent", List.of()),
  NOT_PER_CONSENT("Not exactly as per consent", List.of()),
  NEVER_LAID("Work not completed in the consent timeframe", List.of(PipelineStatus.IN_SERVICE, PipelineStatus.RETURNED_TO_SHORE)),
  NOT_LAID_CONSENT_TIMEFRAME("Not laid in the consent timeframe", List.of()),
  NOT_PROVIDED("Not provided", List.of());

  private final String displayName;
  private final List<PipelineStatus> nonSupportedPipelineStatuses;
  private final boolean isActive;

  AsBuiltNotificationStatus(String displayName,
                            List<PipelineStatus> nonSupportedPipelineStatuses,
                            boolean isActive) {
    this.displayName = displayName;
    this.nonSupportedPipelineStatuses = nonSupportedPipelineStatuses;
    this.isActive = isActive;
  }

  AsBuiltNotificationStatus(String displayName,
                            List<PipelineStatus> nonSupportedPipelineStatuses) {
    this(displayName, nonSupportedPipelineStatuses, true);
  }

  public String getDisplayName() {
    return displayName;
  }

  public List<PipelineStatus> getNonSupportedPipelineStatuses() {
    return nonSupportedPipelineStatuses;
  }

  private static Stream<AsBuiltNotificationStatus> getActiveStatusesStream() {
    return Arrays.stream(AsBuiltNotificationStatus.values())
        .filter(asBuiltNotificationStatus -> asBuiltNotificationStatus.isActive);
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

}